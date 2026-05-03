package com.areswayne.flowdesk.domain.timeentry;

import com.areswayne.flowdesk.domain.timeentry.dto.ManualEntryRequest;
import com.areswayne.flowdesk.domain.timeentry.dto.StartTimerRequest;
import com.areswayne.flowdesk.domain.timeentry.dto.TimeEntryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping("/start")
    public ResponseEntity<TimeEntryResponse> startTimer(@Valid @RequestBody StartTimerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryService.startTimer(request));
    }

    @PostMapping("/stop")
    public ResponseEntity<TimeEntryResponse> stopTimer() {
        return ResponseEntity.ok(timeEntryService.stopTimer());
    }

    @PostMapping("/manual")
    public ResponseEntity<TimeEntryResponse> addManual(@Valid @RequestBody ManualEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryService.addManual(request));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TimeEntryResponse>> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(timeEntryService.getByTask(taskId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TimeEntryResponse>> getMyEntries() {
        return ResponseEntity.ok(timeEntryService.getMyEntries());
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID entryId) {
        timeEntryService.delete(entryId);
        return ResponseEntity.noContent().build();
    }
}