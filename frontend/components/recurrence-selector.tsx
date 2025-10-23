"use client"

import { useEffect, useState, useRef } from "react";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { RRule } from "rrule";
import { Checkbox } from "./ui/checkbox";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Button } from "./ui/button";

interface RecurrenceSelectorProps {
    // Callback when recurrence changes
    onChange: (recurrenceData: RecurrenceData) => void;

    // Initial values (for editing existing tasks)
    initialValue?: RecurrenceData;

    // Optional: disable the component
    disabled?: boolean;
}

export interface RecurrenceData {
    isRecurring: boolean;
    recurrenceRuleStr: string | null;
    startDate: string | null;
    endDate: string | null;
}

const WEEKDAYS = [
    { label: 'Su', value: 0, rrule: RRule.SU },
    { label: 'Mo', value: 1, rrule: RRule.MO },
    { label: 'Tu', value: 2, rrule: RRule.TU },
    { label: 'We', value: 3, rrule: RRule.WE },
    { label: 'Th', value: 4, rrule: RRule.TH },
    { label: 'Fr', value: 5, rrule: RRule.FR },
    { label: 'Sa', value: 6, rrule: RRule.SA },
]

export function RecurrenceSelector({ onChange, initialValue, disabled }: RecurrenceSelectorProps) {
    // Track if we're in the initial mount/initialization phase
    const isInitializing = useRef(true);

    // Track the last loaded initialValue to detect actual changes
    const lastLoadedValue = useRef<string | null>(null);

    // Initialize state from initialValue to handle component remounts
    const getInitialFrequency = () => {
        if (!initialValue?.isRecurring || !initialValue?.recurrenceRuleStr) return 'WEEKLY';
        try {
            const rruleString = initialValue.recurrenceRuleStr.startsWith('RRULE:')
                ? initialValue.recurrenceRuleStr
                : `RRULE:${initialValue.recurrenceRuleStr}`;
            const rule = RRule.fromString(rruleString);
            if (rule.options.freq === RRule.DAILY) return 'DAILY';
            if (rule.options.freq === RRule.WEEKLY) return 'WEEKLY';
            if (rule.options.freq === RRule.MONTHLY) return 'MONTHLY';
        } catch (e) {
            console.error('Error parsing initial RRULE:', e);
        }
        return 'WEEKLY';
    };

    // Enable/Disable recurrence
    const [isRecurring, setIsRecurring] = useState(initialValue?.isRecurring ?? false);
    // Pattern: 'DAILY' | 'WEEKLY' | 'MONTHLY'
    const [frequency, setFrequency] = useState(getInitialFrequency());
    // Interval
    const [interval, setInterval] = useState(1);
    // Selected days
    const [selectedDays, setSelectedDays] = useState<number[]>([]);
    // Date boundaries
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    // local to ISO formatter
    const localToISO = (localString: string) => {
        if(!localString) return null;
        return new Date(localString).toISOString();
    }

    // ISO to local formatter
    // Set dates
    const isoToLocal = (isoString: string) => {
        const date = new Date(isoString);
        let isoStr = date.toISOString();
        isoStr = isoStr.substring(0,16);
        return isoStr;
        
    }

    // Generate RRule
    const generateRRule = () => {
        if (!isRecurring || !endDate || !startDate) return null;

        
        try {
            // Map frequency to rrule.Frequency
            const freqMap: { [key:string]: number } = {
                'DAILY' : RRule.DAILY,
                'WEEKLY'  : RRule.WEEKLY,
                'MONTHLY' : RRule.MONTHLY,
            };

            // Build options object
            type RRuleOptions = {
                freq : number;
                interval: number;
                dtstart: Date;
                until: Date;
                byweekday?: any;
            }

            const options: RRuleOptions = {
                freq: freqMap[frequency],
                interval: interval,
                dtstart: new Date(startDate),
                until: new Date(endDate)
            };

            if(frequency === 'WEEKLY' && selectedDays.length > 0) {
                options.byweekday = selectedDays.map(day => WEEKDAYS[day].rrule);
            }

            const ruleString = new RRule(options).toString().split("\n").find(line => line.startsWith('RRULE:'));

            // Remove the "RRULE:" prefix for backend compatibility (ical4j library expects just the rule part)
            const rule = ruleString ? ruleString.replace('RRULE:', '') : null;

            return rule;

        } catch(error) {
            console.log('Error generating RRULE:', error);
            return null;
        }

    }

    // Toggle day
    const toggleDay = (dayValue: number) => {
        // use setSelectedDays with callback to get previous state
        setSelectedDays(prev => {
            // Is this day alr in the array
            if(prev.includes(dayValue)) {
                return prev.filter(d => d !== dayValue);
            } else {
            // Add it and sort the array
                return [...prev, dayValue].sort();
            }
            
        })
    };

    // Handle frequency change
    const handleFrequencyChange = (newFreq: string) => {
        // Step 1: Update the frequency
        // Need to cast to specific type because newFreq is just a string
        setFrequency(newFreq as 'DAILY' | 'WEEKLY' | 'MONTHLY');

        // Step 2: Clear days if not weekly
        if(newFreq !== 'WEEKLY') {
            setSelectedDays([]); // Empty array = no days selected
        }
    }

    // Notify parent when values changes
    useEffect(() => {
        // Skip during initialization to prevent triggering onChange during mount
        if (isInitializing.current) {
            console.log('RecurrenceSelector: Skipping onChange (initializing)');
            isInitializing.current = false;
            return;
        }

        console.log('RecurrenceSelector: onChange triggered', { isRecurring, frequency, interval, selectedDays, startDate, endDate });

        if (!isRecurring) {
            onChange({
                isRecurring: false,
                recurrenceRuleStr: null,
                startDate: null,
                endDate: null,
            });
            return;
        }

        // Validate required fields
        if(!startDate || !endDate) {
            // Wait for users to fill required fields
            console.log('RecurrenceSelector: Missing required fields');
            return;
        }


        // Generate RRULE and call onChange
        const rrule = generateRRule();
        console.log('RecurrenceSelector: Generated RRULE:', rrule);

        // Call parent's onChange
        onChange({
            isRecurring: true,
            recurrenceRuleStr: rrule ?? null,
            startDate: localToISO(startDate),
            endDate: localToISO(endDate),
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isRecurring, frequency, interval, selectedDays, startDate, endDate]);


    // Editing existing task - only load when initialValue actually changes
    useEffect(() => {
        if(!initialValue) return;

        // Create a stable key from the actual data we care about
        const currentValueKey = JSON.stringify({
            isRecurring: initialValue.isRecurring,
            recurrenceRuleStr: initialValue.recurrenceRuleStr,
            startDate: initialValue.startDate,
            endDate: initialValue.endDate
        });

        // Skip if we've already loaded this exact data
        if (lastLoadedValue.current === currentValueKey) {
            console.log('RecurrenceSelector: Skipping reload - same data');
            return;
        }

        console.log('RecurrenceSelector: Loading NEW initial value', initialValue);
        lastLoadedValue.current = currentValueKey;

        // Mark as initializing to prevent onChange from firing during data load
        isInitializing.current = true;

        if (initialValue.isRecurring) {
            // Set isRecurring
            setIsRecurring(true);

            // Parse RRULE string
            if(initialValue.recurrenceRuleStr) {
                // Backend stores without "RRULE:" prefix, so add it for parsing
                const rruleString = initialValue.recurrenceRuleStr.startsWith('RRULE:')
                    ? initialValue.recurrenceRuleStr
                    : `RRULE:${initialValue.recurrenceRuleStr}`;
                const rule = RRule.fromString(rruleString);

                // Extract frequency
                if(rule.options.freq === RRule.DAILY) {
                    setFrequency('DAILY');
                } else if(rule.options.freq === RRule.WEEKLY) {
                    setFrequency('WEEKLY');
                } else if(rule.options.freq === RRule.MONTHLY) {
                    setFrequency('MONTHLY');
                } else {
                    // Fallback to WEEKLY if frequency is unknown
                    console.warn('Unknown frequency:', rule.options.freq);
                    setFrequency('WEEKLY');
                }

                // Extract interval
                setInterval(rule.options.interval || 1);

                // Extract days (for weekly)
                if (rule.options.byweekday) {
                    // Convert rrule weekdays to numbers
                    const byweekdayArray = Array.isArray(rule.options.byweekday)
                        ? rule.options.byweekday
                        : [rule.options.byweekday];

                    const dayNumbers = byweekdayArray.map((wd) => {
                        // RRule: SU=6, MO=0, TU=1, WE=2, TH=3, FR=4, SA=5
                        // Our WEEKDAYS: Su=0, Mo=1, Tu=2, We=3, Th=4, Fr=5, Sa=6
                        const weekdayNum = typeof wd === 'number' ? wd : (wd as any).weekday;
                        // Convert: RRule day + 1, with Sunday (6) wrapping to 0
                        return (weekdayNum + 1) % 7;
                    })

                    setSelectedDays(dayNumbers);
                }
            }

            if (initialValue.startDate) setStartDate(isoToLocal(initialValue.startDate));
            if (initialValue.endDate) setEndDate(isoToLocal(initialValue.endDate));
        } else {
            // Reset to default values for non-recurring tasks
            setIsRecurring(false);
            setFrequency('WEEKLY');
            setInterval(1);
            setSelectedDays([]);
            setStartDate('');
            setEndDate('');
        }

        console.log('RecurrenceSelector: Initial value loaded, will enable onChange after timeout');
        // After all state updates are queued, allow onChange to fire on next user interaction
        // Use setTimeout to ensure this runs after all the setState calls above
        setTimeout(() => {
            isInitializing.current = false;
            console.log('RecurrenceSelector: Initialization complete, onChange enabled');
        }, 100); // Increased timeout to be safe

    }, [initialValue]);

    return(
        <div className="space-y-4">
            {/* Checkbox to enable */}
            <div className="flex items-center space-x-2">
                <Checkbox 
                    checked={isRecurring} 
                    onCheckedChange={(checked) => setIsRecurring(checked === true)}
                    disabled={disabled}
                    id="recurring-checkbox"
                />
                <Label htmlFor="recurring-checkbox" className="cursor-pointer font-medium">Make this task recurring</Label>
            </div>
            {isRecurring && (
                <>
                {/* All the recurrence options go here */}

                {/* Frequency selector */}
                <div className="space-y-2">
                    <Label>Repeat Pattern</Label>
                    <Select
                        value={frequency}
                        onValueChange={handleFrequencyChange}
                        disabled={disabled}
                    >
                        <SelectTrigger>
                            <SelectValue/>
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="DAILY">Daily</SelectItem>
                            <SelectItem value="WEEKLY">Weekly</SelectItem>
                            <SelectItem value="MONTHLY">Monthly</SelectItem>
                        </SelectContent>
                    </Select>
                </div>

                {/* Every X weeks */}
                <div className="space-y-2">
                    <Label>Every</Label>
                    <div className="flex items-center gap-2">
                        <Input
                            type="number"
                            value={interval}
                            onChange={(e) => setInterval(Math.max(1, parseInt(e.target.value) || 1))}
                            min={1}
                            max={52}
                            disabled={disabled}
                            className="w-20"
                        />
                        <span className="text-sm text-muted-foreground">
                            {frequency.toLowerCase()}{interval > 1 ? 's' : ''}
                        </span>
                    </div>
                </div>

                {/* Choosing the day of the week to repeat */}
                {frequency === 'WEEKLY' && (
                    <div className="space-y-2">
                        <Label>On these days</Label>
                        <div className="flex gap-2">
                            {WEEKDAYS.map((day) => (
                                <Button
                                    key={day.value}
                                    variant={selectedDays.includes(day.value) ? "default" : "outline"}
                                    type="button"
                                    size="sm"
                                    onClick={() => toggleDay(day.value)}
                                    disabled={disabled}
                                    className="w-10 h-10 p-0"
                                >
                                    {day.label}
                                </Button>
                            ))}
                        </div>
                        {selectedDays.length === 0 && (
                            <p className="text-xs text-destructive">
                                Please select at least one day
                            </p>
                        )}
                    </div>
                )}

                {/* Start Date */}
                <div className="space-y-2">
                    <Label htmlFor="start-date">Start Date</Label>
                    <Input
                        id="start-date"
                        type="datetime-local"
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                        disabled={disabled}
                        required
                    />
                </div>

                {/* End Date */}
                <div className="space-y-2">
                    <Label htmlFor="end-date">End Date</Label>
                    <Input
                        id="end-date"
                        type="datetime-local"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        disabled={disabled}
                        required
                    />
                    {startDate && endDate && new Date(endDate) <= new Date(startDate) && (
                        <p className="text-xs text-destructive">
                            End date must be after start date
                        </p>
                    )}
                </div>

                {/* Preview */}
                {startDate && endDate && (
                    <div className="text-xs text-muted-foreground bg-muted p-2 rounded">
                        <strong>Recurrence:</strong> Every {interval} {frequency.toLowerCase()}{interval > 1 ? 's' : ''}
                        {frequency === 'WEEKLY' && selectedDays.length > 0 && (
                            <> on {selectedDays.map(d => WEEKDAYS[d].label).join(', ')}</>
                        )}
                        {' '}from {new Date(startDate).toLocaleDateString()} to {new Date(endDate).toLocaleDateString()}
                    </div>
                )}
                </>
            )}
                




        </div>
    );

}