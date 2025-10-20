package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.service.RecurrenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurrenceServiceImpl implements RecurrenceService {

    @Override
    public List<OffsetDateTime> generateOccurrence(String rruleStr, OffsetDateTime start, OffsetDateTime end) throws RuntimeException {
        try {
            DateTime startDate = new DateTime(Date.from(start.toInstant()));
            DateTime endDate = new DateTime(Date.from(end.toInstant()));

            Recur recur = new Recur(rruleStr);
            DateList dateList = recur.getDates(startDate, endDate, Value.DATE_TIME);
            return dateList.stream()
                    .map(date -> OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error generating Occurrence: " + e.getMessage(), e);
        }

    }
}
