/** @type {import("next").NextConfig} */
const nextConfig = {
  output: "export", // Enables static exports
  // Optional: Add other configurations here if needed later
  // For example, to handle images from external sources if you add them:
  // images: {
  //   unoptimized: true, // If deploying to environments that don't support Next.js Image Optimization (like basic static hosts)
  // },
};

export default nextConfig;

