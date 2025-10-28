"use client";

import React, { useState } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ChartOptions,
} from 'chart.js';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { TimeAnalyticsReportDto } from '@/types/report';
import { Building2, FolderOpen } from 'lucide-react';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

interface TimeAnalyticsBarChartProps {
  data: TimeAnalyticsReportDto;
  loading?: boolean;
}

type ViewMode = 'department' | 'project';

export function TimeAnalyticsBarChart({ data, loading = false }: TimeAnalyticsBarChartProps) {
  const [viewMode, setViewMode] = useState<ViewMode>('department');

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Time Analytics</CardTitle>
          <CardDescription>Logged hours breakdown</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Loading chart...</div>
        </CardContent>
      </Card>
    );
  }

  if (!data) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Time Analytics</CardTitle>
          <CardDescription>Logged hours breakdown</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">No time data available</div>
        </CardContent>
      </Card>
    );
  }

  // Prepare data based on view mode
  const sourceData = viewMode === 'department' ? data.hoursByDepartment : data.hoursByProject;
  const hasData = sourceData && Object.keys(sourceData).length > 0;

  if (!hasData) {
    return (
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <div>
            <CardTitle>Time Analytics</CardTitle>
            <CardDescription>
              Logged hours by {viewMode} - Total: {data.totalHours?.toFixed(2) || '0.00'} hours
            </CardDescription>
          </div>
          <div className="flex space-x-2">
            <Button
              variant={viewMode === 'department' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setViewMode('department')}
            >
              <Building2 className="h-4 w-4 mr-1" />
              Department
            </Button>
            <Button
              variant={viewMode === 'project' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setViewMode('project')}
            >
              <FolderOpen className="h-4 w-4 mr-1" />
              Project
            </Button>
          </div>
        </CardHeader>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">
            No {viewMode} time data available
          </div>
        </CardContent>
      </Card>
    );
  }

  const labels = Object.keys(sourceData);
  const values = Object.values(sourceData).map(value => parseFloat(value.toString()));

  const chartData = {
    labels,
    datasets: [
      {
        label: 'Hours Logged',
        data: values,
        backgroundColor: 'hsl(221, 83%, 53%)',
        borderColor: 'hsl(221, 83%, 47%)',
        borderWidth: 1,
        borderRadius: 4,
        borderSkipped: false,
        hoverBackgroundColor: 'hsl(221, 83%, 57%)',
        hoverBorderColor: 'hsl(221, 83%, 41%)',
      },
    ],
  };

  const options: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const value = context.parsed.y;
            return `${value != null ? value.toFixed(2) : '0.00'} hours`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return `${value}h`;
          },
        },
        title: {
          display: true,
          text: 'Hours',
        },
      },
      x: {
        title: {
          display: true,
          text: viewMode === 'department' ? 'Department' : 'Project',
        },
        ticks: {
          maxRotation: 45,
          minRotation: 0,
        },
      },
    },
  };

  // Calculate statistics
  const maxHours = Math.max(...values);
  const avgHours = values.length > 0 ? values.reduce((a, b) => a + b, 0) / values.length : 0;
  const maxLabel = labels[values.indexOf(maxHours)];

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div>
          <CardTitle>Time Analytics</CardTitle>
          <CardDescription>
            Logged hours by {viewMode} - Total: {data.totalHours?.toFixed(2) || '0.00'} hours
          </CardDescription>
        </div>
        <div className="flex space-x-2">
          <Button
            variant={viewMode === 'department' ? 'default' : 'outline'}
            size="sm"
            onClick={() => setViewMode('department')}
          >
            <Building2 className="h-4 w-4 mr-1" />
            Department
          </Button>
          <Button
            variant={viewMode === 'project' ? 'default' : 'outline'}
            size="sm"
            onClick={() => setViewMode('project')}
          >
            <FolderOpen className="h-4 w-4 mr-1" />
            Project
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Chart */}
          <div className="lg:col-span-3 h-64">
            <Bar data={chartData} options={options} />
          </div>
          
          {/* Statistics */}
          <div className="space-y-4">
            <div className="text-center p-4 bg-muted rounded-lg">
              <div className="text-2xl font-bold text-primary">
                {values.length}
              </div>
              <div className="text-sm text-muted-foreground">
                {viewMode === 'department' ? 'Departments' : 'Projects'}
              </div>
            </div>
            
            {maxHours > 0 && (
              <div className="text-center p-4 bg-muted rounded-lg">
                <div className="text-lg font-semibold">
                  {maxHours.toFixed(2)}h
                </div>
                <div className="text-sm text-muted-foreground">
                  Highest ({maxLabel})
                </div>
              </div>
            )}
            
            {avgHours > 0 && (
              <div className="text-center p-4 bg-muted rounded-lg">
                <div className="text-lg font-semibold">
                  {avgHours.toFixed(2)}h
                </div>
                <div className="text-sm text-muted-foreground">
                  Average
                </div>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
