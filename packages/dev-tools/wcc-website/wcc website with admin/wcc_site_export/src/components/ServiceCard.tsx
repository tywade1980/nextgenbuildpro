import React from 'react';
// import Image from 'next/image'; // Removed unused import
import Link from 'next/link';

interface ServiceCardProps {
  title: string;
  description: string;
  // imageUrl: string; // Placeholder URL for now - removed as unused
  iconUrl?: string; // Placeholder for icon if needed
  linkUrl: string; // Link for 'Explore More'
}

// Removed imageUrl from props destructuring
const ServiceCard: React.FC<ServiceCardProps> = ({ title, description, iconUrl, linkUrl }) => {
  // TODO: Refine styling to match screenshots (shadows, borders, fonts, colors)
  // TODO: Implement icon overlay if needed and assets are available
  // TODO: Use actual Image component when assets are available
  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 flex flex-col">
      <div className="relative h-48 w-full">
        {/* Placeholder for Image */}
        <div className="absolute inset-0 bg-gray-300 flex items-center justify-center text-gray-500">
          Image: {title}
        </div>
        {/* <Image src={imageUrl} alt={title} layout="fill" objectFit="cover" /> */}
        {/* Optional Icon Placeholder */}
        {iconUrl && (
          <div className="absolute top-4 left-4 bg-white p-2 rounded-full shadow">
            {/* <Image src={iconUrl} alt={`${title} icon`} width={24} height={24} /> */}
            <span className="text-xs">Icon</span>
          </div>
        )}
      </div>
      <div className="p-6 flex flex-col flex-grow">
        <h3 className="text-xl font-semibold mb-2 text-gray-800">{title}</h3>
        <p className="text-gray-600 mb-4 flex-grow">{description}</p>
        <Link href={linkUrl} className="mt-auto inline-block bg-orange-100 text-orange-700 hover:bg-orange-200 font-medium py-2 px-4 rounded text-center transition duration-300 self-start">
          EXPLORE MORE
        </Link>
      </div>
    </div>
  );
};

export default ServiceCard;

