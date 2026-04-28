# Construction AI - Complete Business Management Solution

Smart call screen and receptionist dialer, 3 AI models, AI image generation, and comprehensive construction business management solution (CBMS).

## Features

### 🏗️ Construction Business Management
- **Project Management**: Complete project lifecycle tracking with budgets, timelines, tasks, and client relationships
- **Client Management**: Full client directory with contact information, project history, and communication tracking  
- **Call Center Management**: AI-powered call screening with automatic transcription, sentiment analysis, and priority assignment
- **Market Intelligence**: Integration for construction market data, labor rates, and material costs

### 🤖 AI Integration
- **Multi-Model Support**: GPT-4, Claude, and Whisper AI models
- **Smart Call Analysis**: Automatic sentiment detection and priority assignment
- **Real-time Model Testing**: Interactive testing with construction-specific prompts
- **Configurable Parameters**: Adjustable model settings and status monitoring

### 🎨 AI Image Generation
- **Construction-Themed Images**: Generate endless variety of construction-themed content
- **DALL-E 3 Integration**: High-quality AI image generation for proposals and marketing
- **Image Modification**: Built-in tools for brightness, contrast, and style adjustments
- **Batch Generation**: Create multiple variations with different styles and perspectives

### 📱 Complete User Interface
- **Professional Dashboard**: Comprehensive business metrics and real-time activity monitoring
- **Responsive Design**: Mobile-friendly interface built with Tailwind CSS
- **Interactive Components**: Modern UI with proper loading states and error handling
- **Intuitive Navigation**: Easy access to all major features and workflows

## Technical Stack

- **Next.js 15** with App Router and TypeScript
- **AI Integration**: OpenAI API for GPT-4 and DALL-E 3
- **Database**: Prisma with SQLite for data management
- **UI Framework**: Tailwind CSS with Radix UI components
- **State Management**: Zustand for client-side state
- **Image Processing**: Built-in modification and optimization tools

## Getting Started

This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

### Prerequisites

1. **Node.js** (v20.19.5 or higher)
2. **npm** (10.8.2 or higher)

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd next-genai
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Set up environment variables (optional):
   ```bash
   cp .env.example .env.local
   ```
   
   Add your API keys:
   ```bash
   # OpenAI Configuration (optional)
   OPENAI_API_KEY=your_openai_api_key_here
   
   # OpenRouter Configuration (optional)
   OPENROUTER_API_KEY=your_openrouter_api_key_here
   
   # App Configuration
   NEXT_PUBLIC_APP_URL=http://localhost:3000
   ```

4. Initialize the database:
   ```bash
   npm run db:push
   ```

5. Run the development server:
   ```bash
   npm run dev
   ```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

### AI Model Integration

#### OpenRouter Integration
This application supports OpenRouter API to access hundreds of AI models through a unified interface.

**Setup:**
1. Get an API key from [OpenRouter](https://openrouter.ai/)
2. Add it to your `.env.local` file as `OPENROUTER_API_KEY`
3. Go to the AI Models page in the application
4. Click "Sync OpenRouter Models" to fetch available models
5. Configure and use OpenRouter models alongside OpenAI models

**Features:**
- Automatic model discovery and syncing
- Support for various model types (LLM, embedding, classification)
- Construction-specific capability inference
- Pricing and context length information
- Seamless fallback to mock responses when APIs are unavailable

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
