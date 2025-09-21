"use client";
import Image from "next/image";
import { ThemeToggle } from "@/components/theme-toggle";
import { useEffect } from "react";
import { useCurrentUser } from "@/contexts/user-context";
import { useRouter } from "next/navigation";
import { Route } from "@/enums/Route";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";

export default function Home() {
  const router = useRouter();
  const { currentUser, isLoading: isUserLoading } = useCurrentUser();

  // Redirect authenticated users at layout level
  useEffect(() => {
    if (!isUserLoading && !currentUser) {
      router.push(Route.SignIn);
    } else {
      router.push(Route.Dashboard);
    }
  }, [isUserLoading, currentUser]);

  if (isUserLoading) {
    return <FullPageSpinnerLoader />;
  }

  return null;
}
