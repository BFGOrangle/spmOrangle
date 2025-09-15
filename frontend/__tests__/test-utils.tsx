import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, RenderOptions } from '@testing-library/react'
import { ReactElement, ReactNode } from 'react'

// Create a custom render function that includes QueryClient
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Don't retry in tests
        staleTime: Infinity, // Prevent automatic refetching
      },
      mutations: {
        retry: false,
      },
    },
  })

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  queryClient?: QueryClient
}

const AllTheProviders = ({
  children,
  queryClient = createTestQueryClient(),
}: {
  children: ReactNode
  queryClient?: QueryClient
}) => {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

const customRender = (
  ui: ReactElement,
  { queryClient, ...options }: CustomRenderOptions = {}
) => {
  const Wrapper = ({ children }: { children: ReactNode }) => (
    <AllTheProviders queryClient={queryClient}>{children}</AllTheProviders>
  )

  return render(ui, { wrapper: Wrapper, ...options })
}

// Re-export everything
export * from '@testing-library/react'
export { customRender as render, createTestQueryClient }