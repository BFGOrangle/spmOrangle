"use client";

import { useQuery } from "@tanstack/react-query";

// Example API function
const fetchUsers = async () => {
  const response = await fetch("https://jsonplaceholder.typicode.com/users");
  if (!response.ok) {
    throw new Error("Failed to fetch users");
  }
  return response.json();
};

interface User {
  id: number;
  name: string;
  email: string;
  username: string;
}

export function ExampleComponent() {
  const {
    data: users,
    isLoading,
    error,
    isError,
    refetch,
  } = useQuery<User[]>({
    queryKey: ["users"], // Unique key for this query
    queryFn: fetchUsers, // Function that returns the data
    staleTime: 5 * 60 * 1000, // Data is considered fresh for 5 minutes
    retry: 2, // Retry failed requests 2 times
  });

  if (isLoading) {
    return <div>Loading users...</div>;
  }

  if (isError) {
    return (
      <div>
        <p>Error: {error?.message || "Something went wrong"}</p>
        <button onClick={() => refetch()}>Try Again</button>
      </div>
    );
  }

  return (
    <div>
      <h2>Users Example</h2>
      <button onClick={() => refetch()}>Refresh Data</button>
      <ul>
        {users?.map((user) => (
          <li key={user.id}>
            <strong>{user.name}</strong> ({user.username}) - {user.email}
          </li>
        ))}
      </ul>
    </div>
  );
}
