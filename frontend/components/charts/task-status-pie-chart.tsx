"use client";

import React from 'react';
import { Pie } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  ChartOptions,
} from 'chart.js';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { TaskSummaryReportDto } from '@/types/report';
import { CheckCircle2, Clock, AlertCircle, Square } from 'lucide-react';

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend);

interface TaskStatusPieChartProps {
  data: TaskSummaryReportDto;
  loading?: boolean;
}

export function TaskStatusPieChart({ data, loading = false }: TaskStatusPieChartProps) {
  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Task Status Breakdown</CardTitle>
          <CardDescription>Distribution of tasks by status</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Loading chart...</div>
        </CardContent>
      </Card>
    );
  }

  if (!data || data.totalTasks === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Task Status Breakdown</CardTitle>
          <CardDescription>Distribution of tasks by status</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">No task data available</div>
        </CardContent>
      </Card>
    );
  }

  const chartData = {
    labels: ['Completed', 'In Progress', 'To Do', 'Blocked'],
    datasets: [
      {
        data: [
          data.completedTasks || 0,
          data.inProgressTasks || 0,
          data.todoTasks || 0,
          data.blockedTasks || 0,
        ],
        backgroundColor: [
          'hsl(142, 76%, 36%)', // Green for completed
          'hsl(221, 83%, 53%)', // Blue for in progress
          'hsl(47, 96%, 53%)',  // Yellow for todo
          'hsl(0, 84%, 60%)',   // Red for blocked
        ],
        borderColor: [
          'hsl(142, 76%, 30%)',
          'hsl(221, 83%, 47%)',
          'hsl(47, 96%, 47%)',
          'hsl(0, 84%, 54%)',
        ],
        borderWidth: 2,
        hoverBackgroundColor: [
          'hsl(142, 76%, 40%)',
          'hsl(221, 83%, 57%)',
          'hsl(47, 96%, 57%)',
          'hsl(0, 84%, 64%)',
        ],
      },
    ],
  };

  const options: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 20,
          usePointStyle: true,
          font: {
            size: 12,
          },
        },
      },
      tooltip: {
        callbacks: {
          label: function(context: any) {
            const label = context.label || '';
            const value = context.parsed;
            const percentage = data.totalTasks > 0 ? ((value / data.totalTasks) * 100).toFixed(1) : '0.0';
            return `${label}: ${value} tasks (${percentage}%)`;
          },
        },
      },
    },
  };

  const statusStats = [
    {
      label: 'Completed',
      value: data.completedTasks || 0,
      percentage: data.completedPercentage || 0,
      icon: CheckCircle2,
      color: 'text-green-600',
    },
    {
      label: 'In Progress',
      value: data.inProgressTasks || 0,
      percentage: data.inProgressPercentage || 0,
      icon: Clock,
      color: 'text-blue-600',
    },
    {
      label: 'To Do',
      value: data.todoTasks || 0,
      percentage: data.todoPercentage || 0,
      icon: Square,
      color: 'text-yellow-600',
    },
    {
      label: 'Blocked',
      value: data.blockedTasks || 0,
      percentage: data.blockedPercentage || 0,
      icon: AlertCircle,
      color: 'text-red-600',
    },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Task Status Breakdown</CardTitle>
        <CardDescription>
          Distribution of {data.totalTasks} total tasks by status
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Chart */}
          <div className="h-64">
            <Pie data={chartData} options={options} />
          </div>
          
          {/* Statistics */}
          <div className="space-y-4">
            {statusStats.map((stat) => {
              const Icon = stat.icon;
              return (
                <div key={stat.label} className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Icon className={`h-4 w-4 ${stat.color}`} />
                    <span className="text-sm font-medium">{stat.label}</span>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-semibold">{stat.value}</div>
                    <div className="text-xs text-muted-foreground">
                      {stat.percentage.toFixed(1)}%
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
