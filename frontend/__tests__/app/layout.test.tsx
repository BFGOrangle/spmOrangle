import { render, screen } from '@testing-library/react'
import RootLayout from '../../app/layout'

// Mock the query-client module
jest.mock('../../lib/query-client', () => ({
  QueryProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}))

describe('RootLayout', () => {
  it('should render children within the layout', () => {
    render(
      <RootLayout>
        <div>Test Content</div>
      </RootLayout>
    )

    expect(screen.getByText('Test Content')).toBeInTheDocument()
  })

  it('should have proper HTML structure', () => {
    const { container } = render(
      <RootLayout>
        <div>Test Content</div>
      </RootLayout>
    )

    const htmlElement = container.parentElement?.parentElement
    expect(htmlElement?.tagName).toBe('HTML')
    expect(htmlElement?.getAttribute('lang')).toBe('en')
  })
})