import React from 'react';
import Link from 'next/link';

const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-800 text-gray-300 py-8 mt-12">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Contact Information */}
          <div>
            <h3 className="text-xl font-semibold mb-4 text-white">Contact Us</h3>
            <p className="mb-2">
              <span className="font-semibold">Phone:</span> <a href="tel:6143597218" className="hover:text-white">(614) 359-7218</a>
            </p>
            <p className="mb-2">
              <span className="font-semibold">Email:</span> <a href="mailto:tyler@dublinremodelingservices.net" className="hover:text-white">tyler@dublinremodelingservices.net</a>
            </p>
            <p className="mb-2">
              <span className="font-semibold">Service Area:</span> Dublin and surrounding areas
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-xl font-semibold mb-4 text-white">Quick Links</h3>
            <ul className="space-y-2">
              <li><Link href="/" className="hover:text-white">Home</Link></li>
              <li><Link href="/about" className="hover:text-white">About Us</Link></li>
              <li><Link href="/services" className="hover:text-white">Services</Link></li>
              <li><Link href="/portfolio" className="hover:text-white">Portfolio</Link></li>
              <li><Link href="/contact" className="hover:text-white">Contact</Link></li>
            </ul>
          </div>

          {/* Social Media & CTA */}
          <div>
            <h3 className="text-xl font-semibold mb-4 text-white">Connect With Us</h3>
            <div className="flex space-x-4 mb-4">
              <a href="#" className="text-gray-300 hover:text-white">
                <span className="sr-only">Facebook</span>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path fillRule="evenodd" d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" clipRule="evenodd" />
                </svg>
              </a>
              <a href="#" className="text-gray-300 hover:text-white">
                <span className="sr-only">Google Business</span>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path fillRule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1.41 16.09V13.5h2.82v4.59c-1.05.51-2.26.51-2.82 0zm5.51-1.32c-.73.22-1.4.32-2.1.32v-2.67h4.01v.5c0 .79-.71 1.56-1.91 1.85zm-8.6 0c-1.2-.29-1.91-1.06-1.91-1.85v-.5h4.01v2.67c-.7 0-1.37-.1-2.1-.32zm10.09-6.95h-4.18v2.51H9.09V9.82H4.91c-.39 0-.64.23-.64.57v1.85c0 2.05 2.1 3.93 5.27 4.35.17.04.34.04.51.04.17 0 .34 0 .51-.04 3.17-.42 5.27-2.3 5.27-4.35v-1.85c0-.34-.25-.57-.64-.57z" clipRule="evenodd" />
                </svg>
              </a>
            </div>
            <Link href="/contact?estimate=true" className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-2 px-4 rounded inline-block">
              FREE ESTIMATE
            </Link>
          </div>
        </div>
        
        <div className="border-t border-gray-700 mt-8 pt-6 text-center">
          <p>&copy; {new Date().getFullYear()} Wade Custom Carpentry LLC. All Rights Reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
