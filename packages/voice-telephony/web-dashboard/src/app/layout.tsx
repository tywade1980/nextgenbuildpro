import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Next GenAI - Construction Business Management",
  description: "Smart call screen and receptionist dialer with 3 AI models and construction business management solution",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased font-sans">
        {children}
      </body>
    </html>
  );
}
