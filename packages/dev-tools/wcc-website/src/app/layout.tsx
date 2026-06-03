import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/components/Header"; // Import the Header component
import Footer from "@/components/Footer"; // Import the Footer component

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

// Update metadata for the website
export const metadata: Metadata = {
  title: "Wade Custom Carpentry - Crafting Dreams, Building Homes",
  description: "Expert home remodeling, custom architecture, and detailed molding services. Serving the Columbus, OH area with over 25 years of experience.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased flex flex-col min-h-screen`}
      >
        <Header /> {/* Add the Header component */}
        <main className="flex-grow container mx-auto px-6 py-8">
          {/* Page content will be rendered here */}
          {children}
        </main>
        <Footer /> {/* Add the Footer component */}
      </body>
    </html>
  );
}

