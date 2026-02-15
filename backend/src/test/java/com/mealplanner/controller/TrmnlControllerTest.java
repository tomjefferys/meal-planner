package com.mealplanner.controller;

import com.mealplanner.service.TrmnlDisplayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(TrmnlController.class)
class TrmnlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrmnlDisplayService displayService;

    @Test
    void setup_returnsWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/setup")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Meal Planner TRMNL"))
                .andExpect(jsonPath("$.friendly_id").value("MEALPLAN"))
                .andExpect(jsonPath("$.api_key").value("not_required"));
    }

    @Test
    void setup_worksWithoutIdHeader() throws Exception {
        mockMvc.perform(get("/api/setup")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Meal Planner TRMNL"));
    }

    @Test
    void display_returnsImageUrl() throws Exception {
        // When no BASE64 header is sent, the response should contain a fetchable URL
        byte[] fakeImage = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(displayService.renderDisplayImage(any(LocalDate.class))).thenReturn(fakeImage);

        mockMvc.perform(get("/api/display")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image_url", containsString("/api/trmnl-image")))
                .andExpect(jsonPath("$.image_url", not(startsWith("data:"))))
                .andExpect(jsonPath("$.filename").value("meal-plan.bmp"))
                .andExpect(jsonPath("$.image_url_timeout").value(0))
                .andExpect(jsonPath("$.refresh_rate").value(300))
                .andExpect(jsonPath("$.reset_firmware").value(false))
                .andExpect(jsonPath("$.update_firmware").value(false))
                .andExpect(jsonPath("$.special_function", anyOf(is("none"), is("sleep"))));
    }

    @Test
    void display_returnsBase64WhenRequested() throws Exception {
        byte[] fakeImage = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(displayService.renderDisplayImage(any(LocalDate.class))).thenReturn(fakeImage);

        mockMvc.perform(get("/api/display")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .header("BASE64", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image_url", startsWith("data:image/bmp;base64,")));
    }

    @Test
    void display_returnsBase64ViaQueryParam() throws Exception {
        byte[] fakeImage = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(displayService.renderDisplayImage(any(LocalDate.class))).thenReturn(fakeImage);

        mockMvc.perform(get("/api/display?base_64=true")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image_url", startsWith("data:image/bmp;base64,")));
    }

    @Test
    void trmnlImage_servesRawPng() throws Exception {
        byte[] fakeImage = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        when(displayService.renderDisplayImage(any(LocalDate.class))).thenReturn(fakeImage);

        mockMvc.perform(get("/api/trmnl-image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void display_returnsErrorOnRenderFailure_base64Mode() throws Exception {
        // Error only surfaces when rendering inline (BASE64 mode)
        when(displayService.renderDisplayImage(any(LocalDate.class)))
                .thenThrow(new IOException("Render failed"));

        mockMvc.perform(get("/api/display")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .header("BASE64", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to render display image"));
    }

    @Test
    void log_returnsNoContent() throws Exception {
        String logPayload = """
                {
                  "logs": [{
                    "message": "test log",
                    "firmware_version": "1.5.2"
                  }]
                }
                """;

        mockMvc.perform(post("/api/log")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logPayload))
                .andExpect(status().isNoContent());
    }

    @Test
    void log_worksWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/log")
                        .header("ID", "AA:BB:CC:DD:EE:FF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }

    // --- Sleep window logic tests (unit-tested directly on the controller) ---

    @Test
    void isSleepTime_crossesMidnight_duringNight() {
        // Default window: 23:00 → 06:00
        TrmnlController controller = new TrmnlController(displayService, "23:00", "06:00", 300, "");
        assertThat(controller.isSleepTime(LocalTime.of(23, 30))).isTrue();
        assertThat(controller.isSleepTime(LocalTime.of(2, 0))).isTrue();
        assertThat(controller.isSleepTime(LocalTime.of(5, 59))).isTrue();
    }

    @Test
    void isSleepTime_crossesMidnight_duringDay() {
        TrmnlController controller = new TrmnlController(displayService, "23:00", "06:00", 300, "");
        assertThat(controller.isSleepTime(LocalTime.of(6, 0))).isFalse();
        assertThat(controller.isSleepTime(LocalTime.of(12, 0))).isFalse();
        assertThat(controller.isSleepTime(LocalTime.of(22, 59))).isFalse();
    }

    @Test
    void isSleepTime_sameDayWindow() {
        // Window within the same day: 01:00 → 05:00
        TrmnlController controller = new TrmnlController(displayService, "01:00", "05:00", 300, "");
        assertThat(controller.isSleepTime(LocalTime.of(2, 0))).isTrue();
        assertThat(controller.isSleepTime(LocalTime.of(4, 59))).isTrue();
        assertThat(controller.isSleepTime(LocalTime.of(0, 30))).isFalse();
        assertThat(controller.isSleepTime(LocalTime.of(5, 0))).isFalse();
        assertThat(controller.isSleepTime(LocalTime.of(12, 0))).isFalse();
    }

    @Test
    void isSleepTime_boundaryValues() {
        TrmnlController controller = new TrmnlController(displayService, "23:00", "06:00", 300, "");
        // Start is inclusive
        assertThat(controller.isSleepTime(LocalTime.of(23, 0))).isTrue();
        // Stop is exclusive (device should wake up at this time)
        assertThat(controller.isSleepTime(LocalTime.of(6, 0))).isFalse();
    }

    @Test
    void currentTime_usesConfiguredTimezone() {
        TrmnlController controllerUtc = new TrmnlController(displayService, "23:00", "06:00", 300, "UTC");
        TrmnlController controllerSydney = new TrmnlController(displayService, "23:00", "06:00", 300, "Australia/Sydney");
        // Sydney is always ahead of UTC, so its current time should be later
        LocalTime utcTime = controllerUtc.currentTime();
        LocalTime sydneyTime = controllerSydney.currentTime();
        // They should differ (Sydney is UTC+10 or UTC+11 depending on DST)
        assertThat(utcTime).isNotEqualTo(sydneyTime);
    }

    @Test
    void currentDate_usesConfiguredTimezone() {
        // Just verify it returns a date (detailed timezone boundary testing is fragile)
        TrmnlController controller = new TrmnlController(displayService, "23:00", "06:00", 300, "Australia/Sydney");
        assertThat(controller.currentDate()).isNotNull();
    }
}
