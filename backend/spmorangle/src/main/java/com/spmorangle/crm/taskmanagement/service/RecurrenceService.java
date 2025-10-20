package com.spmorangle.crm.taskmanagement.service;

import java.time.OffsetDateTime;
import java.util.List;

public interface RecurrenceService {
    List<OffsetDateTime> generateOccurrence(String rruleStr,OffsetDateTime start, OffsetDateTime end);
}
