/** @type {import('next').NextConfig} */
const nextConfig = {
  devIndicators: false,
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
    NEXT_PUBLIC_MOCK_MODE: process.env.NEXT_PUBLIC_MOCK_MODE || 'false',
  },
};

module.exports = nextConfig;
