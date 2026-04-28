import Link from 'next/link';
// import Image from 'next/image'; // Removed unused import
import ServiceCard from '@/components/ServiceCard';
import ContactForm from '@/components/ContactForm'; // Import ContactForm

// --- Data Definitions ---
const services = [
  { title: "Home Remodeling", description: "Transform your living spaces with our expert remodeling services, tailored to your style and needs. From complete room renovations to specific area updates, we bring your vision to life with quality craftsmanship.", imageUrl: "/placeholder-service-1.jpg", iconUrl: "/icon-home.svg", linkUrl: "/services/home-remodeling" },
  { title: "Custom Architecture", description: "Design unique architectural features that enhance your home's functionality and aesthetic appeal. Our custom solutions include built-ins, unique room layouts, and distinctive structural elements that make your space truly yours.", imageUrl: "/placeholder-service-2.jpg", iconUrl: "/icon-architecture.svg", linkUrl: "/services/custom-architecture" },
  { title: "Molding and Trim Work", description: "Add elegance with our precise molding and trim installations for a refined look. Our detailed craftsmanship in crown molding, baseboards, wainscoting, and decorative trim elements elevates any room's appearance.", imageUrl: "/placeholder-service-3.jpg", iconUrl: "/icon-trim.svg", linkUrl: "/services/molding-trim" },
  { title: "Decorative Enhancements", description: "Elevate your home with custom decorative elements that reflect your personal taste and style. From custom shelving and mantels to unique ceiling treatments and architectural details that make your space stand out.", imageUrl: "/placeholder-service-4.jpg", iconUrl: "/icon-decorative.svg", linkUrl: "/services/decorative-enhancements" },
  { title: "Bathroom Remodeling", description: "Create a modern, functional bathroom with our comprehensive remodeling solutions and expert craftsmanship. We handle everything from layout changes and fixture updates to complete transformations with luxury features.", imageUrl: "/placeholder-service-5.jpg", iconUrl: "/icon-bathroom.svg", linkUrl: "/services/bathroom-remodeling" },
  { title: "Kitchen Remodeling", description: "Revamp your kitchen with our professional remodeling services, blending style and practicality seamlessly. We create functional, beautiful kitchens with custom cabinetry, quality countertops, and thoughtful layouts.", imageUrl: "/placeholder-service-6.jpg", iconUrl: "/icon-kitchen.svg", linkUrl: "/services/kitchen-remodeling" }
];

const galleryImages = [
  { id: 1, src: "/placeholder-gallery-1.jpg", alt: "Custom Closet System" },
  { id: 2, src: "/placeholder-gallery-2.jpg", alt: "Entryway Built-ins" },
  { id: 3, src: "/placeholder-gallery-3.jpg", alt: "Bathroom Remodel" },
  { id: 4, src: "/placeholder-gallery-4.jpg", alt: "Outdoor Living Space" },
  { id: 5, src: "/placeholder-gallery-5.jpg", alt: "Landscaping Project" },
  { id: 6, src: "/placeholder-gallery-6.jpg", alt: "Custom Woodworking" },
];

const testimonials = [
  {
    id: 1,
    quote: "Wade Custom Carpentry exceeded our expectations with their attention to detail and quality craftsmanship. Our custom closet system has transformed our master bedroom, and the team was professional from start to finish. Truly a top-notch experience!",
    name: "Michael Thompson",
    location: "Dublin, Ohio"
  }
];
// --- End Data Definitions ---

export default function Home() {
  // TODO: Replace placeholder background/images/icons with actual assets
  // TODO: Refine typography and colors to match screenshots exactly
  // TODO: Implement actual logo in Header
  // TODO: Add 'Why Choose Us' section from screenshots
  // TODO: Use actual Image component when assets are available

  return (
    <>
      {/* Hero Section */}
      <section 
        className="relative bg-gray-600 text-white py-32 px-6 text-center" 
        style={{ 
          backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/placeholder-hero.jpg')`, 
          backgroundSize: 'cover', 
          backgroundPosition: 'center' 
        }}
      >
         <div className="container mx-auto">
          <p className="text-sm uppercase tracking-widest mb-2 text-gray-300">Transforming Spaces Beautifully</p>
          <h1 className="text-4xl md:text-6xl font-bold mb-4">Crafting Dreams, Building Homes</h1>
          <p className="text-lg md:text-xl mb-8 max-w-3xl mx-auto text-gray-200">
            With 25 years of expertise, Wade Custom Carpentry specializes in transforming homes with custom remodeling, architecture, and decorative craftsmanship.
          </p>
          <div className="flex justify-center gap-4">
            <Link href="/contact?estimate=true" className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-3 px-6 rounded transition duration-300">
              GET STARTED
            </Link>
            <Link href="/portfolio" className="border-2 border-white hover:bg-white hover:text-gray-800 text-white font-bold py-3 px-6 rounded transition duration-300">
              CHECK PROJECTS
            </Link>
          </div>
        </div>
      </section>

      {/* About Us Section */}
      <section className="py-16 px-6 bg-white">
         <div className="container mx-auto grid md:grid-cols-2 gap-12 items-center">
          <div className="relative h-80 rounded-lg overflow-hidden shadow-lg">
            <div className="absolute inset-0 bg-gray-300 flex items-center justify-center text-gray-500">
              About Us Image Placeholder
            </div>
          </div>
          <div>
            <p className="text-sm uppercase tracking-widest mb-2 text-gray-500">About Us</p>
            <h2 className="text-3xl font-bold mb-4 text-gray-800">Bringing Expertise and Passion to Every Project</h2>
            <p className="text-gray-600 mb-6">
              At Wade Custom Carpentry, we bring over 25 years of experience to every project, ensuring top-quality craftsmanship and personalized service. Led by Tyler, our skilled team is dedicated to transforming your home with precision and creativity. From custom remodeling and architectural work to intricate molding and decorative details, we handle every job with care and expertise. Our commitment to excellence and customer satisfaction makes us the ideal choice for your next home improvement project.
            </p>
            <div className="bg-orange-100 border-l-4 border-orange-500 text-orange-700 p-4 rounded-md shadow">
              <p className="font-semibold">5% Senior & Military Discount</p>
              <p>Enjoy a 5% discount on all services as a token of our appreciation for seniors and military members.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="py-16 px-6 bg-gray-50">
        <div className="container mx-auto">
          <p className="text-sm uppercase tracking-widest mb-2 text-center text-gray-500">Services</p>
          <h2 className="text-3xl font-bold mb-4 text-center text-gray-800">Expert Services for Your Home</h2>
          <p className="text-center text-gray-600 mb-12 max-w-3xl mx-auto">
            Our services include expert home remodeling, custom architecture, detailed molding, and more. With 25 years of experience, we deliver high-quality results tailored to your specific needs.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {services.map((service) => (
              // Pass necessary props, excluding imageUrl for now
              <ServiceCard key={service.title} title={service.title} description={service.description} iconUrl={service.iconUrl} linkUrl={service.linkUrl} />
            ))}
          </div>
        </div>
      </section>

      {/* Gallery Section (Homepage Preview) */}
      <section className="py-16 px-6 bg-white">
        <div className="container mx-auto text-center">
          <p className="text-sm uppercase tracking-widest mb-2 text-gray-500">Gallery</p>
          <h2 className="text-3xl font-bold mb-4 text-gray-800">Showcasing Our Craftsmanship and Projects</h2>
          <p className="text-gray-600 mb-12 max-w-3xl mx-auto">
            Explore our gallery to see the high-quality craftsmanship and creative designs we've brought to life. Each project highlights our commitment to excellence and attention to detail.
          </p>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-8">
            {galleryImages.slice(0, 6).map(image => (
              <div key={image.id} className="relative aspect-square rounded-lg overflow-hidden shadow-md bg-gray-300">
                <div className="absolute inset-0 flex items-center justify-center text-gray-500">
                  {image.alt}
                </div>
              </div>
            ))}
          </div>
          <Link href="/portfolio" className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-3 px-6 rounded transition duration-300 inline-block">
            EXPLORE MORE
          </Link>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="py-16 px-6 bg-gray-100">
        <div className="container mx-auto text-center max-w-4xl">
          <p className="text-sm uppercase tracking-widest mb-2 text-gray-500">Testimonial</p>
          <h2 className="text-3xl font-bold mb-8 text-gray-800">Customer Feedback and Success Stories</h2>
          {testimonials.map(testimonial => (
            <div key={testimonial.id} className="bg-white p-8 rounded-lg shadow-lg relative">
              <span className="absolute top-4 left-4 text-6xl text-orange-300 opacity-50 font-serif">"</span>
              <blockquote className="text-gray-600 italic text-lg mb-4">
                {testimonial.quote}
              </blockquote>
              <p className="font-semibold text-gray-800">{testimonial.name}</p>
              <p className="text-sm text-gray-500">{testimonial.location}</p>
              <span className="absolute bottom-4 right-4 text-6xl text-orange-300 opacity-50 font-serif">"</span>
            </div>
          ))}
        </div>
      </section>

      {/* Contact Section */}
      <section className="py-16 px-6 bg-white">
        <div className="container mx-auto">
           <div className="text-center mb-12">
             <p className="text-sm uppercase tracking-widest mb-2 text-gray-500">Contact Us</p>
             <h2 className="text-3xl font-bold mb-4 text-gray-800">Reach out for personalized service and expert advice on your project.</h2>
           </div>
          <ContactForm />
        </div>
      </section>
    </>
  );
}
