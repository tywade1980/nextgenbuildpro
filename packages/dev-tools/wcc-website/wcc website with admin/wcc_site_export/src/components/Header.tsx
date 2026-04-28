import React from 'react';
import Link from 'next/link';

const Header: React.FC = () => {
  // TODO: Replace placeholder logo with actual logo component or image
  // TODO: Refine styling to match screenshots more closely (colors, fonts, spacing)
  return (
    <header className="bg-gray-800 text-white shadow-md">
      <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
        {/* Logo Placeholder */}
        <div>
          <Link href="/" className="text-xl font-bold hover:text-gray-300">
            Wade Custom Carpentry
          </Link>
        </div>

        {/* Navigation Links */}
        <div className="hidden md:flex items-center space-x-4">
          <Link href="/" className="py-2 px-3 hover:text-gray-300">Home</Link>
          <Link href="/about" className="py-2 px-3 hover:text-gray-300">About Us</Link>
          <Link href="/services" className="py-2 px-3 hover:text-gray-300">Services</Link>
          {/* Assuming '/gallery' will be the portfolio page */}
          <Link href="/portfolio" className="py-2 px-3 hover:text-gray-300">Gallery</Link>
          <Link href="/testimonials" className="py-2 px-3 hover:text-gray-300">Testimonial</Link>
          <Link href="/contact" className="py-2 px-3 hover:text-gray-300">Contact Us</Link>
          <Link href="/contact?estimate=true" className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-2 px-4 rounded">
            FREE ESTIMATE
          </Link>
        </div>

        {/* Mobile Menu Button (Placeholder) */}
        <div className="md:hidden">
          <button className="text-white focus:outline-none">
            {/* Icon placeholder */}
            <svg className="h-6 w-6" fill="none" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" viewBox="0 0 24 24" stroke="currentColor"><path d="M4 6h16M4 12h16m-7 6h7"></path></svg>
          </button>
        </div>
      </nav>
    </header>
  );
};

export default Header;

