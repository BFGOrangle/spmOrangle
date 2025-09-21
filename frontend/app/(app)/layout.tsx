"use client";
import { useEffect } from "react";
import { useCurrentUser } from "@/contexts/user-context";
import { useRouter } from "next/navigation";
import { Route } from "@/enums/Route";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";

export default function Layout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { currentUser, isLoading: isUserLoading } = useCurrentUser();

  // Redirect authenticated users
  useEffect(() => {
    if (!isUserLoading && !currentUser) {
      router.push(Route.SignIn);
    }
  }, [isUserLoading, currentUser]);

  if (isUserLoading) {
    return <FullPageSpinnerLoader />;
  }

  return (
    <>
      <SidebarProvider>
        {!isUserLoading && currentUser && (
          <>
            <AppSidebar />
            <SidebarTrigger />
          </>
        )}
        {children}
      </SidebarProvider>
    </>
  );
}
