import http from "./http";

// SCR-CALENDAR-001 · UC4·UC5 (선택 흐름) Google Calendar 연동
export function fetchCalendarEvents() {
  return http.get("/calendar/events");
}

export function saveSelectedAnniversary(payload) {
  return http.post("/calendar/anniversaries", payload);
}

// UC13 준비 일정 Calendar 저장
export function savePrepEventToCalendar(payload) {
  return http.post("/calendar/prep-events", payload);
}
