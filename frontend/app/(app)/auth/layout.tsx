"use client";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";
import { useCurrentUser } from "@/contexts/user-context";
import { Route } from "@/enums/Route";
import { RefreshCcwDot } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function Layout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { currentUser, isLoading } = useCurrentUser();

  // Redirect authenticated users at layout level
  useEffect(() => {
    if (!isLoading && currentUser) {
      router.push(Route.Dashboard);
    }
  }, [isLoading, currentUser]);

  // Show loading while checking auth OR while user is authenticated (redirecting)
  if (isLoading || currentUser) {
    return (
      <FullPageSpinnerLoader
        loadingMessage={currentUser ? "Redirecting..." : ""}
      />
    );
  }

  // Only render auth pages if user is definitely not authenticated
  return (
    <main className="flex items-center justify-center md:h-screen w-full">
      <div className="relative mx-auto flex w-full max-w-[400px] flex-col space-y-2.5 p-4 md:-mt-32">
        <div className="flex w-full items-end rounded-lg p-3 md:h-36"></div>
        <div className="flex flex-col items-center mb-8 pb-2">
          <a
            href="/"
            className="flex items-center gap-2 mb-4 hover:opacity-80 transition-opacity"
          >
            <RefreshCcwDot className="h-8 w-8 text-blue-600" />
            <h1 className="text-2xl font-bold text-gray-900">Sync Up</h1>
          </a>
          <p className="text-gray-600 text-center">Task Management System</p>
        </div>
        {children}
      </div>
    </main>
  );
}
