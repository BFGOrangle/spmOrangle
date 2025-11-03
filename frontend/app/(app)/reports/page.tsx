"use client";

import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  FileText,
  Download,
  BarChart3,
  Clock,
  CheckCircle2,
  AlertCircle,
  Loader2,
  Users
} from "lucide-react";
import { StaffBreakdownTable } from "@/components/staff-breakdown-table";
import { TaskStatusPieChart } from "@/components/charts/task-status-pie-chart";
import { TimeAnalyticsBarChart } from "@/components/charts/time-analytics-bar-chart";
import { reportService } from "@/services/report-service";
import {
  ReportFilterDto,
  TaskSummaryReportDto,
  TimeAnalyticsReportDto,
  StaffBreakdownDto,
  ProjectOptionDto,
  TimeRange,
  ReportFormat,
  ComprehensiveReportDto,
} from "@/types/report";
import { useToast } from "@/hooks/use-toast";
import { useCurrentUser } from "@/contexts/user-context";
import { useRouter } from "next/navigation";
import { Route } from "@/enums/Route";

export default function ReportsPage() {
  const { toast } = useToast();
  const { isAdmin, isLoading: isUserLoading } = useCurrentUser();
  const router = useRouter();

  // Filter state - matching backend request body fields
  const [department, setDepartment] = useState<string>("");
  const [projectIds, setProjectIds] = useState<number[]>([]);
  const [timeRange, setTimeRange] = useState<TimeRange>("MONTHLY");
  const [format, setFormat] = useState<ReportFormat>("JSON");
  const [startDate, setStartDate] = useState<string>(
    new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0]
  );
  const [endDate, setEndDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );

  // Data state
  const [departments, setDepartments] = useState<string[]>([]);
  const [projects, setProjects] = useState<ProjectOptionDto[]>([]);
  const [taskSummary, setTaskSummary] = useState<TaskSummaryReportDto | null>(null);
  const [timeAnalytics, setTimeAnalytics] = useState<TimeAnalyticsReportDto | null>(null);
  const [staffBreakdown, setStaffBreakdown] = useState<StaffBreakdownDto[]>([]);

  // UI state
  const [loading, setLoading] = useState(false);
  const [loadingFilters, setLoadingFilters] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Redirect non-HR users away from reports page (only HR/Admin can access)
  useEffect(() => {
    if (!isUserLoading && !isAdmin) {
      toast({
        title: "Access Denied",
        description: "You do not have permission to access reports.",
        variant: "destructive",
      });
      router.push(Route.Dashboard);
    }
  }, [isAdmin, isUserLoading, router, toast]);

  // Load initial filter options
  useEffect(() => {
    const loadFilterOptions = async () => {
      try {
        setLoadingFilters(true);
        const [depts, projs] = await Promise.all([
          reportService.getAvailableDepartments(),
          reportService.getAvailableProjects(),
        ]);
        setDepartments(depts);
        setProjects(projs);
      } catch (err) {
        console.error("Error loading filter options:", err);
        toast({
          title: "Error",
          description: "Failed to load filter options",
          variant: "destructive",
        });
      } finally {
        setLoadingFilters(false);
      }
    };

    loadFilterOptions();
  }, [toast]);

  // Load projects when department changes
  useEffect(() => {
    if (department) {
      const loadProjectsByDepartment = async () => {
        try {
          const projs = await reportService.getAvailableProjects(department);
          setProjects(projs);
          // Reset selected projects when department changes
          setProjectIds([]);
        } catch (err) {
          console.error("Error loading projects:", err);
          toast({
            title: "Error",
            description: "Failed to load projects for department",
            variant: "destructive",
          });
        }
      };
      loadProjectsByDepartment();
    }
  }, [department, toast]);

  // Handle report generation
  const handleGenerateReport = async () => {
    try {
      setLoading(true);
      setError(null);

      // Validate required dates
      if (!startDate || !endDate) {
        toast({
          title: "Validation Error",
          description: "Start date and end date are required",
          variant: "destructive",
        });
        setLoading(false);
        return;
      }

      const filters: ReportFilterDto = {
        department: department || undefined,
        projectIds: projectIds.length > 0 ? projectIds : undefined,
        timeRange,
        format,
        startDate,
        endDate,
      };

      // If format is PDF or CSV, download the file
      if (format === "PDF" || format === "CSV") {
        const blob = await reportService.generateReport(filters) as Blob;
        const filename = reportService.generateReportFilename(filters);
        reportService.downloadBlob(blob, filename);
        
        toast({
          title: "Report Downloaded",
          description: `Your ${format} report has been downloaded successfully.`,
        });
      } else {
        // For JSON format, the generate endpoint returns complete data
        // It automatically includes both taskSummary and timeAnalytics
        const reportData = await reportService.generateReport(filters) as ComprehensiveReportDto;
        
        setTaskSummary(reportData.taskSummary);
        setTimeAnalytics(reportData.timeAnalytics);
        setStaffBreakdown(reportData.staffBreakdown || []);
        
        toast({
          title: "Report Generated",
          description: "Your report has been generated successfully.",
        });
      }
    } catch (err) {
      console.error("Error generating report:", err);
      setError("Failed to generate report. Please try again.");
      toast({
        title: "Error",
        description: "Failed to generate report",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const isCustomTimeRange = timeRange === "CUSTOM";

  // Don't render the page if user is loading or is not an HR user
  if (isUserLoading || !isAdmin) {
    return null;
  }

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <div className="flex items-center gap-2">
          <BarChart3 className="h-5 w-5" />
          <h1 className="text-lg font-semibold">Reports</h1>
        </div>
      </header>

      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        {/* Filter Section */}
        <Card>
          <CardHeader>
            <CardTitle>Report Filters</CardTitle>
            <CardDescription>
              Select filters to generate scoped reports
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              {/* Department Filter */}
              <div className="space-y-2">
                <Label htmlFor="department">Department</Label>
                <Select
                  value={department || "ALL_DEPARTMENTS"}
                  onValueChange={(value) => setDepartment(value === "ALL_DEPARTMENTS" ? "" : value)}
                  disabled={loadingFilters}
                >
                  <SelectTrigger id="department">
                    <SelectValue placeholder="All Departments" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ALL_DEPARTMENTS">All Departments</SelectItem>
                    {departments.map((dept) => (
                      <SelectItem key={dept} value={dept}>
                        {dept}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Project Filter */}
              <div className="space-y-2">
                <Label htmlFor="project">Projects</Label>
                <Select
                  value={projectIds[0]?.toString() || "ALL_PROJECTS"}
                  onValueChange={(value) =>
                    setProjectIds(value === "ALL_PROJECTS" ? [] : [parseInt(value)])
                  }
                  disabled={loadingFilters || projects.length === 0}
                >
                  <SelectTrigger id="project">
                    <SelectValue placeholder="All Projects" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ALL_PROJECTS">All Projects</SelectItem>
                    {projects.map((project) => (
                      <SelectItem key={project.id} value={project.id.toString()}>
                        {project.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {projectIds.length > 0 && (
                  <p className="text-xs text-muted-foreground">
                    {projectIds.length} project{projectIds.length !== 1 ? 's' : ''} selected
                  </p>
                )}
              </div>

              {/* Time Range Filter */}
              <div className="space-y-2">
                <Label htmlFor="timeRange">Time Range</Label>
                <Select
                  value={timeRange}
                  onValueChange={(value) => setTimeRange(value as TimeRange)}
                >
                  <SelectTrigger id="timeRange">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="WEEKLY">Weekly</SelectItem>
                    <SelectItem value="MONTHLY">Monthly</SelectItem>
                    <SelectItem value="QUARTERLY">Quarterly</SelectItem>
                    <SelectItem value="YEARLY">Yearly</SelectItem>
                    <SelectItem value="CUSTOM">Custom</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Export Format Filter */}
              <div className="space-y-2">
                <Label htmlFor="exportFormat">Export Format</Label>
                <Select
                  value={format}
                  onValueChange={(value) => setFormat(value as ReportFormat)}
                >
                  <SelectTrigger id="exportFormat">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="JSON">View Online (JSON)</SelectItem>
                    <SelectItem value="PDF">PDF</SelectItem>
                    <SelectItem value="CSV">CSV</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Date Range - Required */}
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="startDate">
                  Start Date
                </Label>
                <Input
                  id="startDate"
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  placeholder="YYYY-MM-DD"
                />
                {!isCustomTimeRange && startDate && (
                  <p className="text-xs text-muted-foreground">
                    Overrides {timeRange.toLowerCase()} range start
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="endDate">
                  End Date
                </Label>
                <Input
                  id="endDate"
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  placeholder="YYYY-MM-DD"
                />
                {!isCustomTimeRange && endDate && (
                  <p className="text-xs text-muted-foreground">
                    Overrides {timeRange.toLowerCase()} range end
                  </p>
                )}
              </div>
            </div>

            {/* Generate Button */}
            <div className="mt-6">
              <Button
                onClick={handleGenerateReport}
                disabled={loading || loadingFilters}
                className="w-full md:w-auto"
              >
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Download className="mr-2 h-4 w-4" />
                    Generate Report
                  </>
                )}
              </Button>
            </div>

            {error && (
              <div className="mt-4 flex items-center gap-2 text-sm text-destructive">
                <AlertCircle className="h-4 w-4" />
                {error}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Report Results - Only show when format is JSON and data is loaded */}
        {format === "JSON" && (taskSummary || timeAnalytics) && (
          <>
            {/* Task Summary Section */}
            {taskSummary && (
              <div className="space-y-4">
                <h2 className="text-2xl font-bold flex items-center gap-2">
                  <FileText className="h-6 w-6" />
                  Task Summary
                </h2>

                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                  <Card data-testid="task-summary-total">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Total Tasks
                      </CardTitle>
                      <BarChart3 className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold" data-testid="task-summary-total-value">
                        {taskSummary.totalTasks}
                      </div>
                    </CardContent>
                  </Card>

                  <Card data-testid="task-summary-completed">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Completed
                      </CardTitle>
                      <CheckCircle2 className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold" data-testid="task-summary-completed-value">
                        {taskSummary.completedTasks}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {taskSummary.completedPercentage?.toFixed(1)}% completion rate
                      </p>
                    </CardContent>
                  </Card>

                  <Card data-testid="task-summary-in-progress">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        In Progress
                      </CardTitle>
                      <Clock className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold" data-testid="task-summary-in-progress-value">
                        {taskSummary.inProgressTasks}
                      </div>
                    </CardContent>
                  </Card>

                  <Card data-testid="task-summary-blocked">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                      <CardTitle className="text-sm font-medium">
                        Blocked
                      </CardTitle>
                      <AlertCircle className="h-4 w-4 text-red-500" />
                    </CardHeader>
                    <CardContent>
                      <div className="text-2xl font-bold" data-testid="task-summary-blocked-value">
                        {taskSummary.blockedTasks}
                      </div>
                    </CardContent>
                  </Card>
                </div>
                {/* Charts Section */}
                <div className="space-y-4">
                  <h2 className="text-2xl font-bold flex items-center gap-2">
                    <BarChart3 className="h-6 w-6" />
                    Visual Analytics
                  </h2>
                  <div className="grid gap-4 md:grid-cols-2">
                    <TaskStatusPieChart data={taskSummary} loading={loading} />
                    {timeAnalytics && (
                      <TimeAnalyticsBarChart data={timeAnalytics} loading={loading} />
                    )}
                  </div>
                </div>

              </div>

            )}

            {/* Time Analytics Section */}
            {timeAnalytics && (
              <div className="space-y-4">
                <h2 className="text-2xl font-bold flex items-center gap-2">
                  <Clock className="h-6 w-6" />
                  Time Analytics
                </h2>

                <div className="grid gap-4 md:grid-cols-2">
                  <Card>
                    <CardHeader>
                      <CardTitle>Total Time Spent</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="text-3xl font-bold">
                        {timeAnalytics.totalHours?.toFixed(1)} hrs
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>Project Details</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="text-sm text-muted-foreground">
                        {timeAnalytics.projectDetails 
                          ? `${Object.keys(timeAnalytics.projectDetails).length} projects tracked`
                          : 'No project data available'}
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Time by Project */}
                {timeAnalytics.hoursByProject && Object.keys(timeAnalytics.hoursByProject).length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle>Time by Project</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        {Object.entries(timeAnalytics.hoursByProject).map(([projectName, hours]) => (
                          <div
                            key={projectName}
                            className="flex items-center justify-between"
                          >
                            <span className="font-medium">{projectName}</span>
                            <Badge variant="secondary">
                              {Number(hours).toFixed(1)} hrs
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                )}

                {/* Time by Department */}
                {timeAnalytics.hoursByDepartment && Object.keys(timeAnalytics.hoursByDepartment).length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle>Time by Department</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        {Object.entries(timeAnalytics.hoursByDepartment).map(([department, hours]) => (
                          <div
                            key={department}
                            className="flex items-center justify-between"
                          >
                            <span className="font-medium">{department}</span>
                            <Badge variant="secondary">
                              {Number(hours).toFixed(1)} hrs
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            )}

            {/* Staff Breakdown Section */}
            {staffBreakdown && staffBreakdown.length > 0 && (
              <div className="space-y-4">
                <h2 className="text-2xl font-bold flex items-center gap-2">
                  <Users className="h-6 w-6" />
                  Staff Breakdown
                </h2>
                <Card>
                  <CardHeader>
                    <CardTitle>Task Distribution & Hours by Staff</CardTitle>
                    <CardDescription>
                      View task counts by status and logged hours for each staff member
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <StaffBreakdownTable data={staffBreakdown} />
                  </CardContent>
                </Card>
              </div>
            )}
          </>
        )}
      </div>
    </SidebarInset>
  );
}
