package com.mealplanner.controller;

import com.mealplanner.service.TrmnlDisplayService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * TRMNL e-ink display integration endpoints.
 * Implements the BYOS (Bring Your Own Server) firmware API:
 * - GET /api/setup   — initial device setup
 * - GET /api/display — returns the current screen image
 * - GET /api/trmnl-image — serves the raw PNG for the device to fetch
 * - POST /api/log    — accepts device log data
 *
 * @see <a href="https://docs.trmnl.com/go/diy/byos">TRMNL BYOS docs</a>
 */
@RestController
@RequestMapping("/api")
public class TrmnlController {

    private static final Logger log = LoggerFactory.getLogger(TrmnlController.class);

    private final TrmnlDisplayService displayService;
    private final LocalTime sleepStart;
    private final LocalTime sleepStop;
    private final int refreshRate;
    private final ZoneId timezone;

    public TrmnlController(TrmnlDisplayService displayService,
                           @Value("${trmnl.sleep.start:23:00}") String sleepStartStr,
                           @Value("${trmnl.sleep.stop:06:00}") String sleepStopStr,
                           @Value("${trmnl.refresh-rate:300}") int refreshRate,
                           @Value("${trmnl.timezone:}") String timezoneStr) {
        this.displayService = displayService;
        this.sleepStart = LocalTime.parse(sleepStartStr);
        this.sleepStop = LocalTime.parse(sleepStopStr);
        this.refreshRate = refreshRate;
        this.timezone = timezoneStr.isBlank() ? ZoneId.systemDefault() : ZoneId.of(timezoneStr);
    }

    /** Returns the current time in the configured timezone. */
    LocalTime currentTime() {
        return LocalTime.now(timezone);
    }

    /** Returns the current date in the configured timezone. */
    LocalDate currentDate() {
        return LocalDate.now(timezone);
    }

    /**
     * Check whether the given time falls inside the configured sleep window.
     * Handles windows that cross midnight (e.g. 23:00 → 06:00).
     */
    boolean isSleepTime(LocalTime time) {
        if (sleepStart.isBefore(sleepStop)) {
            // Window does NOT cross midnight (e.g. 01:00 → 05:00)
            return !time.isBefore(sleepStart) && time.isBefore(sleepStop);
        } else {
            // Window crosses midnight (e.g. 23:00 → 06:00)
            return !time.isBefore(sleepStart) || time.isBefore(sleepStop);
        }
    }

    /**
     * Setup endpoint — called once when a device is first provisioned.
     * Returns a welcome message and a placeholder image URL.
     */
    @GetMapping("/setup")
    public ResponseEntity<Map<String, Object>> setup(
            @RequestHeader(value = "ID", required = false) String deviceId) {

        log.info("TRMNL setup request from device: {}", deviceId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api_key", "not_required");
        response.put("friendly_id", "MEALPLAN");
        response.put("image_url", "");
        response.put("message", "Welcome to Meal Planner TRMNL");
        return ResponseEntity.ok(response);
    }

    /**
     * Display endpoint — called on each device refresh cycle.
     * If the device sends a BASE64 header (or base_64 query param), the image
     * is returned inline as a base64 data URI. Otherwise a URL pointing to
     * /api/trmnl-image is returned for the device to fetch separately.
     */
    @GetMapping("/display")
    public ResponseEntity<Map<String, Object>> display(
            @RequestHeader(value = "ID", required = false) String deviceId,
            @RequestHeader(value = "BASE64", required = false) String base64Header,
            @RequestParam(value = "base_64", required = false) String base64Param,
            HttpServletRequest request) {

        log.info("TRMNL display request from device: {}, BASE64: {}, headers: {}",
                deviceId, base64Header, logHeaders(request));

        boolean wantsBase64 = "true".equalsIgnoreCase(base64Header)
                || "true".equalsIgnoreCase(base64Param);

        try {
            String imageUrl;
            if (wantsBase64) {
                byte[] imageBytes = displayService.renderDisplayImage(currentDate());
                imageUrl = "data:image/bmp;base64,"
                        + Base64.getEncoder().encodeToString(imageBytes);
            } else {
                // Build an absolute URL the device can fetch the image from
                String baseUrl = request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort();
                imageUrl = baseUrl + "/api/trmnl-image";
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("image_url", imageUrl);
            response.put("filename", "meal-plan.bmp");
            response.put("image_url_timeout", 0);
            response.put("refresh_rate", refreshRate);
            response.put("reset_firmware", false);
            response.put("update_firmware", false);
            response.put("firmware_url", "");
            response.put("special_function", isSleepTime(currentTime()) ? "sleep" : "none");

            log.info("TRMNL display response for {}: image_url={} (base64={})",
                    deviceId, wantsBase64 ? "<inline>" : imageUrl, wantsBase64);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to render TRMNL display image", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to render display image"
            ));
        }
    }

    /**
     * Image endpoint — serves the current meal plan as a raw PNG image.
     * This is the URL returned by /api/display for the device to fetch.
     */
    @GetMapping(value = "/trmnl-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> trmnlImage() throws IOException {
        log.info("TRMNL image fetch");
        byte[] imageBytes = displayService.renderDisplayImage(currentDate());
        return ResponseEntity.ok()
                .contentType(Objects.requireNonNull(MediaType.IMAGE_PNG))
                .body(imageBytes);
    }

    /**
     * Log endpoint — accepts device log/diagnostic data.
     * Logs the information and returns HTTP 204 No Content.
     */
    @PostMapping("/log")
    public ResponseEntity<Void> logEntry(
            @RequestHeader(value = "ID", required = false) String deviceId,
            @RequestBody(required = false) Map<String, Object> logData) {

        log.info("TRMNL log from device {}: {}", deviceId, logData);
        return ResponseEntity.noContent().build();
    }

    /**
     * Preview endpoint — serves the raw PNG image directly in the browser.
     * Not part of the TRMNL firmware API; just for testing/debugging.
     * Optionally accepts a ?date= parameter to preview a specific day.
     *
     * Usage: open http://localhost:8080/api/trmnl-preview in a browser.
     */
    @GetMapping(value = "/trmnl-preview", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> preview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
            throws IOException {

        LocalDate targetDate = date != null ? date : currentDate();
        byte[] imageBytes = displayService.renderDisplayImage(targetDate);

        return ResponseEntity.ok()
                .contentType(Objects.requireNonNull(MediaType.IMAGE_PNG))
                .body(imageBytes);
    }

    /**
     * Collect request headers into a string for debug logging.
     */
    private String logHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder("{");
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            sb.append(name).append("=").append(request.getHeader(name));
            if (names.hasMoreElements()) sb.append(", ");
        }
        return sb.append("}").toString();
    }
}
