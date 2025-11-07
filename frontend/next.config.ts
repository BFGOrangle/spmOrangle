import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  async rewrites() {
    // Note: :path* will replace the matched path without the prefix
    // So /api/notifications becomes /notifications when forwarded
    // We need to keep the /api prefix in the destination
    const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
    const apiDestination = `${baseUrl}/api/:path*`;

    console.log('🔄 Next.js API Proxy configured:');
    console.log('   Source: /api/:path*');
    console.log('   Destination:', apiDestination);

    return [
      {
        source: '/api/:path*',
        destination: apiDestination,
      },
    ];
  },
};

export default nextConfig;