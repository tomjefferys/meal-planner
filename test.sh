#!/usr/bin/env bash
# Run all tests for the Meal Planner project (backend + frontend)

set -e

PASS="\033[0;32m✔\033[0m"
FAIL="\033[0;31m✘\033[0m"
BOLD="\033[1m"
RESET="\033[0m"

echo ""
echo "${BOLD}🍽️  Meal Planner — Running All Tests${RESET}"
echo ""

BACKEND_OK=0
FRONTEND_OK=0

# --- Backend tests (Maven / JUnit) ---
echo "${BOLD}☕ Backend tests (JUnit)${RESET}"
echo "──────────────────────────────────"
cd backend
if ./mvnw test -q 2>&1; then
    echo -e "${PASS} Backend tests passed"
    BACKEND_OK=1
else
    echo -e "${FAIL} Backend tests failed"
fi
cd ..

echo ""

# --- Frontend tests (Vitest) ---
echo "${BOLD}⚛️  Frontend tests (Vitest)${RESET}"
echo "──────────────────────────────────"
cd frontend
npm install --silent 2>/dev/null
if npx vitest run 2>&1; then
    echo -e "${PASS} Frontend tests passed"
    FRONTEND_OK=1
else
    echo -e "${FAIL} Frontend tests failed"
fi
cd ..

# --- Summary ---
echo ""
echo "${BOLD}──────────────────────────────────${RESET}"
if [ $BACKEND_OK -eq 1 ] && [ $FRONTEND_OK -eq 1 ]; then
    echo -e "${PASS} ${BOLD}All tests passed${RESET}"
    exit 0
else
    echo -e "${FAIL} ${BOLD}Some tests failed${RESET}"
    [ $BACKEND_OK  -eq 0 ] && echo -e "   ${FAIL} Backend"
    [ $FRONTEND_OK -eq 0 ] && echo -e "   ${FAIL} Frontend"
    exit 1
fi
