"use client";
import {
  Briefcase,
  CheckSquare,
  Layout,
  LogOut,
  RefreshCcwDot,
  User,
  Calendar,
  BarChart3,
} from "lucide-react";

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import Link from "next/link";
import { Button } from "./ui/button";
import { handleSignOut } from "@/lib/cognito-actions";
import { ThemeToggle } from "./theme-toggle";
import { NotificationBell } from "./notification-bell";
import { useCurrentUser } from "@/contexts/user-context";

// Menu items.
const items = [
  {
    title: "My Analytics",
    url: "/dashboard",
    icon: Layout,
  },
  {
    title: "My Calendar",
    url: "/calendar",
    icon: Calendar,
  },
  {
    title: "Projects",
    url: "/projects",
    icon: Briefcase,
  },
  {
    title: "Tasks",
    url: "/tasks",
    icon: CheckSquare,
  },
  {
    title: "Reports",
    url: "/reports",
    icon: BarChart3,
  },
  {
    title: "My Profile",
    url: "/profile",
    icon: User,
  },
];

export function AppSidebar() {
  const { isStaff } = useCurrentUser();

  // Filter out Reports for STAFF users
  const visibleItems = items.filter((item) => {
    if (item.url === "/reports" && isStaff) {
      return false;
    }
    return true;
  });

  return (
    <Sidebar>
      <SidebarHeader className="p-4">
        <div className="flex items-center gap-2">
          <RefreshCcwDot className="h-6 w-6 text-blue-600 shrink-0" />
          <div className="flex justify-between items-center w-full">
            <p className="font-semibold text-lg">SyncUp</p>
            <div className="flex items-center gap-1">
              <NotificationBell />
              <ThemeToggle />
            </div>
          </div>
        </div>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Application</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {visibleItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <Link href={item.url}>
                      <item.icon />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <Button
          onClick={handleSignOut}
          variant="outline"
          size="sm"
          className="w-full p-2"
          title="Sign Out"
        >
          <LogOut className="h-4 w-4" />
          Sign out
        </Button>
      </SidebarFooter>
    </Sidebar>
  );
}
