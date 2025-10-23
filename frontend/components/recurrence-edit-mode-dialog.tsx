"use client"

import { AlertDialog, AlertDialogDescription, AlertDialogTitle } from "@radix-ui/react-alert-dialog";
import { useState } from "react";
import { AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogFooter, AlertDialogHeader } from "./ui/alert-dialog";
import { Label } from "./ui/label";
import { RadioGroup, RadioGroupItem } from "./ui/radio-group";
import { RecurrenceEditMode } from "@/types/recurrence";

interface RecurrenceEditModeDialogProps {
    // Control visibility
    open: boolean;
    onOpenChange: (open: boolean) => void;

    // Handle user's choice
    onSelect: (mode: RecurrenceEditMode) => void;

}

export function RecurrenceEditModeDialog({ open, onOpenChange, onSelect }: RecurrenceEditModeDialogProps) {
    const [selectedMode, setSelectedMode] = useState<RecurrenceEditMode>('ALL_FUTURE_INSTANCES');

    const handleModeChange = (mode: RecurrenceEditMode) => { 
        setSelectedMode(mode);
     };
    const handleConfirm = () => {
        // Call the parent's callback with the selected mode
        onSelect(selectedMode);

        // Close the dialog
        onOpenChange(false);
    };
    const handleCancel = () => {
        onOpenChange(false);
        setSelectedMode('ALL_FUTURE_INSTANCES');
    };

    return (
        <AlertDialog open={open} onOpenChange={onOpenChange}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>Task Recurrence Settings</AlertDialogTitle>
                </AlertDialogHeader>
                <AlertDialogDescription>
                    This is a recurring task. How would you like to apply your changes?
                </AlertDialogDescription>

                {/* Radio Group Section */}
                <div className="space-y-4">
                    <RadioGroup value={selectedMode} onValueChange={handleModeChange}>
                        <div className="flex items-center gap-3">
                            <RadioGroupItem value="THIS_INSTANCE" id="r1" />
                            <Label htmlFor="r1">Only this instance</Label>
                            <span className="text-xs text-muted-foreground">Changes apply to this task only</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <RadioGroupItem value="THIS_AND_FUTURE_INSTANCES" id="r2" />
                            <Label htmlFor="r2">This and future instances</Label>
                            <span className="text-xs text-muted-foreground">Changes apply from this task onward</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <RadioGroupItem value="ALL_FUTURE_INSTANCES" id="r3" />
                            <Label htmlFor="r3">Future instances only</Label>
                            <span className="text-xs text-muted-foreground">Changes apply to all future tasks</span>
                        </div>
                    </RadioGroup>
                </div>

                <AlertDialogFooter>
                    <AlertDialogCancel onClick={handleCancel}>
                        Cancel
                    </AlertDialogCancel>
                    <AlertDialogAction onClick={handleConfirm}>
                        Apply Changes
                    </AlertDialogAction>
                </AlertDialogFooter>

            </AlertDialogContent>

        </AlertDialog>
    )

}