import { StaffBreakdownDto } from "@/types/report";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

interface StaffBreakdownTableProps {
  data: StaffBreakdownDto[];
}

export function StaffBreakdownTable({ data }: StaffBreakdownTableProps) {
  if (!data || data.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        No staff data available for the selected filters.
      </div>
    );
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Staff Name</TableHead>
            <TableHead>Department</TableHead>
            <TableHead className="text-right">To Do</TableHead>
            <TableHead className="text-right">In Progress</TableHead>
            <TableHead className="text-right">Completed</TableHead>
            <TableHead className="text-right">Blocked</TableHead>
            <TableHead className="text-right">Logged Hours</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {data.map((staff) => (
            <TableRow key={staff.userId}>
              <TableCell className="font-medium">{staff.userName}</TableCell>
              <TableCell>{staff.department}</TableCell>
              <TableCell className="text-right">{staff.todoTasks}</TableCell>
              <TableCell className="text-right">
                {staff.inProgressTasks}
              </TableCell>
              <TableCell className="text-right">
                {staff.completedTasks}
              </TableCell>
              <TableCell className="text-right">{staff.blockedTasks}</TableCell>
              <TableCell className="text-right">
                {staff.loggedHours.toFixed(2)}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

