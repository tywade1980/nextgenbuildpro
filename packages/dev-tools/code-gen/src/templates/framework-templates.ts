/**
 * Framework Templates for Code Generation
 * 
 * Provides pre-configured templates for different frameworks and project types
 */

export interface FrameworkTemplate {
  id: string
  name: string
  description: string
  framework: string
  features: string[]
  files: Record<string, string>
  dependencies: Record<string, string>
  devDependencies: Record<string, string>
  scripts: Record<string, string>
  category: 'frontend' | 'backend' | 'fullstack' | 'mobile'
  tags: string[]
}

export const frameworkTemplates: FrameworkTemplate[] = [
  {
    id: 'react-starter',
    name: 'React Starter',
    description: 'Modern React application with TypeScript and Vite',
    framework: 'react',
    features: ['TypeScript', 'Vite', 'React Router', 'Tailwind CSS'],
    files: {
      'src/App.tsx': `import React from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import About from './pages/About'
import './App.css'

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
        </Routes>
      </div>
    </Router>
  )
}`,
      'src/pages/Home.tsx': `import React from 'react'

export default function Home() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-gray-900 mb-4">Welcome</h1>
      <p className="text-lg text-gray-600">This is your React starter application.</p>
    </div>
  )
}`
    },
    dependencies: {
      'react': '^18.2.0',
      'react-dom': '^18.2.0',
      'react-router-dom': '^6.8.0'
    },
    devDependencies: {
      '@vitejs/plugin-react': '^3.1.0',
      'vite': '^4.1.0',
      'typescript': '^4.9.5'
    },
    scripts: {
      'dev': 'vite',
      'build': 'tsc && vite build',
      'preview': 'vite preview'
    },
    category: 'frontend',
    tags: ['react', 'typescript', 'vite', 'spa']
  },
  {
    id: 'nextjs-fullstack',
    name: 'Next.js Full Stack',
    description: 'Complete Next.js application with API routes and database',
    framework: 'nextjs',
    features: ['TypeScript', 'API Routes', 'Database', 'Authentication'],
    files: {
      'src/app/page.tsx': `export default function Home() {
  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold mb-4">Next.js Full Stack</h1>
      <p className="text-lg text-gray-600">Your full-stack application is ready!</p>
    </main>
  )
}`,
      'src/app/api/users/route.ts': `import { NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  return NextResponse.json({ users: [] })
}

export async function POST(request: NextRequest) {
  const body = await request.json()
  return NextResponse.json({ message: 'User created', user: body })
}`
    },
    dependencies: {
      'next': '^13.2.1',
      'react': '^18.2.0',
      'react-dom': '^18.2.0'
    },
    devDependencies: {
      'typescript': '^4.9.5',
      '@types/node': '^18.14.6',
      '@types/react': '^18.0.28'
    },
    scripts: {
      'dev': 'next dev',
      'build': 'next build',
      'start': 'next start'
    },
    category: 'fullstack',
    tags: ['nextjs', 'react', 'api', 'ssr']
  },
  {
    id: 'express-api-basic',
    name: 'Express API - Basic',
    description: 'Simple RESTful API server with Express and TypeScript',
    framework: 'express',
    features: ['TypeScript', 'Express', 'CORS', 'Validation'],
    files: {
      'src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import { router } from './routes'

const app = express()
const PORT = process.env.PORT || 3000

app.use(helmet())
app.use(cors())
app.use(morgan('combined'))
app.use(express.json())

app.use('/api', router)

app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() })
})

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`)
})`,
      'src/routes/index.ts': `import { Router } from 'express'

export const router = Router()

router.get('/users', (req, res) => {
  res.json({ users: [] })
})

router.post('/users', (req, res) => {
  res.json({ message: 'User created', user: req.body })
})`
    },
    dependencies: {
      'express': '^4.18.2',
      'cors': '^2.8.5',
      'helmet': '^6.0.1',
      'morgan': '^1.10.0'
    },
    devDependencies: {
      'typescript': '^4.9.5',
      '@types/express': '^4.17.17',
      '@types/cors': '^2.8.13',
      'nodemon': '^2.0.20',
      'ts-node': '^10.9.1'
    },
    scripts: {
      'dev': 'nodemon src/server.ts',
      'build': 'tsc',
      'start': 'node dist/server.js'
    },
    category: 'backend',
    tags: ['express', 'api', 'typescript', 'rest']
  },
  {
    id: 'express-mongodb',
    name: 'Express + MongoDB',
    description: 'Production-ready Express API with MongoDB, authentication, and comprehensive middleware',
    framework: 'express',
    features: ['TypeScript', 'MongoDB', 'JWT Auth', 'Validation', 'Error Handling', 'Rate Limiting', 'API Documentation'],
    files: {
      'src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import rateLimit from 'express-rate-limit'
import { connectDatabase } from './config/database'
import { errorHandler, notFound } from './middleware/errorHandler'
import { authRoutes } from './routes/auth'
import { userRoutes } from './routes/users'
import { productRoutes } from './routes/products'
import swaggerUi from 'swagger-ui-express'
import { swaggerSpec } from './config/swagger'

const app = express()
const PORT = process.env.PORT || 3000

// Security middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
}))

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.'
})
app.use('/api/', limiter)

// CORS configuration
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:3000',
  credentials: true
}))

// Body parsing middleware
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

// Logging
app.use(morgan('combined'))

// API documentation
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec))

// Routes
app.use('/api/auth', authRoutes)
app.use('/api/users', userRoutes)
app.use('/api/products', productRoutes)

// Health check
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    environment: process.env.NODE_ENV || 'development'
  })
})

// Error handling
app.use(notFound)
app.use(errorHandler)

// Database connection and server start
const startServer = async () => {
  try {
    await connectDatabase()
    app.listen(PORT, () => {
      console.log(\`🚀 Server running on port \${PORT}\`)
      console.log(\`📚 API Documentation: http://localhost:\${PORT}/api-docs\`)
      console.log(\`🏥 Health Check: http://localhost:\${PORT}/health\`)
    })
  } catch (error) {
    console.error('Failed to start server:', error)
    process.exit(1)
  }
}

startServer()
`,
      'src/config/database.ts': `import mongoose from 'mongoose'

export const connectDatabase = async (): Promise<void> => {
  try {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/express-api'
    
    await mongoose.connect(mongoUri, {
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 5000,
      socketTimeoutMS: 45000,
    })
    
    console.log('✅ MongoDB connected successfully')
    
    mongoose.connection.on('error', (error) => {
      console.error('MongoDB connection error:', error)
    })
    
    mongoose.connection.on('disconnected', () => {
      console.log('📡 MongoDB disconnected')
    })
    
  } catch (error) {
    console.error('❌ MongoDB connection failed:', error)
    throw error
  }
}

export const disconnectDatabase = async (): Promise<void> => {
  try {
    await mongoose.disconnect()
    console.log('📡 MongoDB disconnected')
  } catch (error) {
    console.error('Error disconnecting from MongoDB:', error)
  }
}`,
      'src/models/User.ts': `import mongoose, { Document, Schema } from 'mongoose'
import bcrypt from 'bcryptjs'

export interface IUser extends Document {
  name: string
  email: string
  password: string
  role: 'user' | 'admin'
  isActive: boolean
  createdAt: Date
  updatedAt: Date
  comparePassword(candidatePassword: string): Promise<boolean>
}

const userSchema = new Schema<IUser>({
  name: {
    type: String,
    required: [true, 'Name is required'],
    trim: true,
    maxlength: [50, 'Name cannot exceed 50 characters']
  },
  email: {
    type: String,
    required: [true, 'Email is required'],
    unique: true,
    lowercase: true,
    match: [/^\\S+@\\S+\\.\\S+$/, 'Please provide a valid email']
  },
  password: {
    type: String,
    required: [true, 'Password is required'],
    minlength: [6, 'Password must be at least 6 characters'],
    select: false
  },
  role: {
    type: String,
    enum: ['user', 'admin'],
    default: 'user'
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true
})

// Hash password before saving
userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next()
  
  try {
    const salt = await bcrypt.genSalt(12)
    this.password = await bcrypt.hash(this.password, salt)
    next()
  } catch (error) {
    next(error as Error)
  }
})

// Compare password method
userSchema.methods.comparePassword = async function(candidatePassword: string): Promise<boolean> {
  return bcrypt.compare(candidatePassword, this.password)
}

// Remove password from JSON output
userSchema.methods.toJSON = function() {
  const userObject = this.toObject()
  delete userObject.password
  return userObject
}

export const User = mongoose.model<IUser>('User', userSchema)`,
      'src/models/Product.ts': `import mongoose, { Document, Schema } from 'mongoose'

export interface IProduct extends Document {
  name: string
  description: string
  price: number
  category: string
  stock: number
  isActive: boolean
  images: string[]
  createdBy: mongoose.Types.ObjectId
  createdAt: Date
  updatedAt: Date
}

const productSchema = new Schema<IProduct>({
  name: {
    type: String,
    required: [true, 'Product name is required'],
    trim: true,
    maxlength: [100, 'Product name cannot exceed 100 characters']
  },
  description: {
    type: String,
    required: [true, 'Product description is required'],
    maxlength: [500, 'Description cannot exceed 500 characters']
  },
  price: {
    type: Number,
    required: [true, 'Price is required'],
    min: [0, 'Price cannot be negative']
  },
  category: {
    type: String,
    required: [true, 'Category is required'],
    enum: ['electronics', 'clothing', 'books', 'home', 'sports', 'other']
  },
  stock: {
    type: Number,
    required: [true, 'Stock quantity is required'],
    min: [0, 'Stock cannot be negative'],
    default: 0
  },
  isActive: {
    type: Boolean,
    default: true
  },
  images: [{
    type: String
  }],
  createdBy: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }
}, {
  timestamps: true
})

// Index for better query performance
productSchema.index({ name: 'text', description: 'text' })
productSchema.index({ category: 1, isActive: 1 })
productSchema.index({ createdBy: 1 })

export const Product = mongoose.model<IProduct>('Product', productSchema)`,
      'src/routes/auth.ts': `import { Router } from 'express'
import { body } from 'express-validator'
import { AuthController } from '../controllers/AuthController'
import { validate } from '../middleware/validate'
import { auth } from '../middleware/auth'

const router = Router()

/**
 * @swagger
 * /api/auth/register:
 *   post:
 *     tags: [Authentication]
 *     summary: Register a new user
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - name
 *               - email
 *               - password
 *             properties:
 *               name:
 *                 type: string
 *               email:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       201:
 *         description: User registered successfully
 *       400:
 *         description: Invalid input data
 */
router.post('/register', [
  body('name')
    .trim()
    .isLength({ min: 2, max: 50 })
    .withMessage('Name must be between 2 and 50 characters'),
  body('email')
    .isEmail()
    .normalizeEmail()
    .withMessage('Please provide a valid email'),
  body('password')
    .isLength({ min: 6 })
    .withMessage('Password must be at least 6 characters long')
], validate, AuthController.register)

/**
 * @swagger
 * /api/auth/login:
 *   post:
 *     tags: [Authentication]
 *     summary: Login user
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: Login successful
 *       401:
 *         description: Invalid credentials
 */
router.post('/login', [
  body('email')
    .isEmail()
    .normalizeEmail()
    .withMessage('Please provide a valid email'),
  body('password')
    .notEmpty()
    .withMessage('Password is required')
], validate, AuthController.login)

/**
 * @swagger
 * /api/auth/me:
 *   get:
 *     tags: [Authentication]
 *     summary: Get current user profile
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: User profile retrieved successfully
 *       401:
 *         description: Unauthorized
 */
router.get('/me', auth, AuthController.getProfile)

/**
 * @swagger
 * /api/auth/refresh:
 *   post:
 *     tags: [Authentication]
 *     summary: Refresh access token
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Token refreshed successfully
 *       401:
 *         description: Invalid token
 */
router.post('/refresh', auth, AuthController.refreshToken)

export { router as authRoutes }`,
      'src/controllers/AuthController.ts': `import { Request, Response } from 'express'
import jwt from 'jsonwebtoken'
import { User } from '../models/User'
import { ApiError } from '../utils/ApiError'
import { catchAsync } from '../utils/catchAsync'

export class AuthController {
  static generateToken = (userId: string): string => {
    return jwt.sign(
      { userId },
      process.env.JWT_SECRET || 'your-secret-key',
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    )
  }

  static register = catchAsync(async (req: Request, res: Response) => {
    const { name, email, password } = req.body

    // Check if user already exists
    const existingUser = await User.findOne({ email })
    if (existingUser) {
      throw new ApiError(400, 'User already exists with this email')
    }

    // Create new user
    const user = await User.create({
      name,
      email,
      password
    })

    // Generate token
    const token = AuthController.generateToken(user._id.toString())

    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: {
        user,
        token
      }
    })
  })

  static login = catchAsync(async (req: Request, res: Response) => {
    const { email, password } = req.body

    // Find user and include password
    const user = await User.findOne({ email, isActive: true }).select('+password')
    if (!user) {
      throw new ApiError(401, 'Invalid email or password')
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password)
    if (!isPasswordValid) {
      throw new ApiError(401, 'Invalid email or password')
    }

    // Generate token
    const token = AuthController.generateToken(user._id.toString())

    res.json({
      success: true,
      message: 'Login successful',
      data: {
        user: user.toJSON(),
        token
      }
    })
  })

  static getProfile = catchAsync(async (req: Request, res: Response) => {
    const user = await User.findById(req.user?.userId)
    if (!user) {
      throw new ApiError(404, 'User not found')
    }

    res.json({
      success: true,
      data: { user }
    })
  })

  static refreshToken = catchAsync(async (req: Request, res: Response) => {
    const userId = req.user?.userId
    if (!userId) {
      throw new ApiError(401, 'Invalid token')
    }

    const user = await User.findById(userId)
    if (!user || !user.isActive) {
      throw new ApiError(401, 'User not found or inactive')
    }

    const token = AuthController.generateToken(userId)

    res.json({
      success: true,
      message: 'Token refreshed successfully',
      data: { token }
    })
  })
}`,
      'src/middleware/auth.ts': `import { Request, Response, NextFunction } from 'express'
import jwt from 'jsonwebtoken'
import { User } from '../models/User'
import { ApiError } from '../utils/ApiError'
import { catchAsync } from '../utils/catchAsync'

declare global {
  namespace Express {
    interface Request {
      user?: {
        userId: string
        role: string
      }
    }
  }
}

export const auth = catchAsync(async (req: Request, res: Response, next: NextFunction) => {
  const token = req.header('Authorization')?.replace('Bearer ', '')

  if (!token) {
    throw new ApiError(401, 'Access token required')
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key') as any
    
    const user = await User.findById(decoded.userId)
    if (!user || !user.isActive) {
      throw new ApiError(401, 'Invalid token or user not found')
    }

    req.user = {
      userId: user._id.toString(),
      role: user.role
    }

    next()
  } catch (error) {
    throw new ApiError(401, 'Invalid token')
  }
})

export const authorize = (...roles: string[]) => {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      throw new ApiError(401, 'Authentication required')
    }

    if (!roles.includes(req.user.role)) {
      throw new ApiError(403, 'Insufficient permissions')
    }

    next()
  }
}`,
      'src/middleware/errorHandler.ts': `import { Request, Response, NextFunction } from 'express'
import { ApiError } from '../utils/ApiError'

export const errorHandler = (
  error: Error,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  let err = { ...error }
  err.message = error.message

  console.error(error)

  // Mongoose bad ObjectId
  if (error.name === 'CastError') {
    const message = 'Resource not found'
    err = new ApiError(404, message)
  }

  // Mongoose duplicate key
  if ((error as any).code === 11000) {
    const message = 'Duplicate field value entered'
    err = new ApiError(400, message)
  }

  // Mongoose validation error
  if (error.name === 'ValidationError') {
    const message = Object.values((error as any).errors).map((val: any) => val.message).join(', ')
    err = new ApiError(400, message)
  }

  // JWT errors
  if (error.name === 'JsonWebTokenError') {
    const message = 'Invalid token'
    err = new ApiError(401, message)
  }

  if (error.name === 'TokenExpiredError') {
    const message = 'Token expired'
    err = new ApiError(401, message)
  }

  res.status((err as ApiError).statusCode || 500).json({
    success: false,
    error: err.message || 'Server Error',
    ...(process.env.NODE_ENV === 'development' && { stack: error.stack })
  })
}

export const notFound = (req: Request, res: Response, next: NextFunction) => {
  const error = new ApiError(404, \`Not found - \${req.originalUrl}\`)
  next(error)
}`,
      'src/middleware/validate.ts': `import { Request, Response, NextFunction } from 'express'
import { validationResult } from 'express-validator'
import { ApiError } from '../utils/ApiError'

export const validate = (req: Request, res: Response, next: NextFunction) => {
  const errors = validationResult(req)
  
  if (!errors.isEmpty()) {
    const errorMessages = errors.array().map(error => error.msg).join(', ')
    throw new ApiError(400, errorMessages)
  }
  
  next()
}`,
      'src/utils/ApiError.ts': `export class ApiError extends Error {
  public statusCode: number
  public isOperational: boolean

  constructor(statusCode: number, message: string, isOperational = true, stack = '') {
    super(message)
    this.statusCode = statusCode
    this.isOperational = isOperational

    if (stack) {
      this.stack = stack
    } else {
      Error.captureStackTrace(this, this.constructor)
    }
  }
}`,
      'src/utils/catchAsync.ts': `import { Request, Response, NextFunction } from 'express'

export const catchAsync = (fn: Function) => {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next)
  }
}`,
      'src/config/swagger.ts': `import swaggerJsdoc from 'swagger-jsdoc'

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Express API with MongoDB',
      version: '1.0.0',
      description: 'A production-ready Express API with MongoDB integration',
      contact: {
        name: 'API Support',
        email: 'support@example.com'
      }
    },
    servers: [
      {
        url: process.env.API_URL || 'http://localhost:3000',
        description: 'Development server'
      }
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        }
      }
    }
  },
  apis: ['./src/routes/*.ts']
}

export const swaggerSpec = swaggerJsdoc(options)`,
      'src/routes/users.ts': `import { Router } from 'express'
import { body, param, query } from 'express-validator'
import { UserController } from '../controllers/UserController'
import { validate } from '../middleware/validate'
import { auth, authorize } from '../middleware/auth'

const router = Router()

/**
 * @swagger
 * /api/users:
 *   get:
 *     tags: [Users]
 *     summary: Get all users (Admin only)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           minimum: 1
 *         description: Page number
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 100
 *         description: Number of items per page
 *     responses:
 *       200:
 *         description: Users retrieved successfully
 *       401:
 *         description: Unauthorized
 *       403:
 *         description: Forbidden - Admin access required
 */
router.get('/', [
  auth,
  authorize('admin'),
  query('page').optional().isInt({ min: 1 }),
  query('limit').optional().isInt({ min: 1, max: 100 })
], validate, UserController.getUsers)

/**
 * @swagger
 * /api/users/{id}:
 *   get:
 *     tags: [Users]
 *     summary: Get user by ID
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: User retrieved successfully
 *       404:
 *         description: User not found
 */
router.get('/:id', [
  auth,
  param('id').isMongoId().withMessage('Invalid user ID')
], validate, UserController.getUserById)

/**
 * @swagger
 * /api/users/{id}:
 *   put:
 *     tags: [Users]
 *     summary: Update user
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *               email:
 *                 type: string
 *     responses:
 *       200:
 *         description: User updated successfully
 *       404:
 *         description: User not found
 */
router.put('/:id', [
  auth,
  param('id').isMongoId().withMessage('Invalid user ID'),
  body('name').optional().trim().isLength({ min: 2, max: 50 }),
  body('email').optional().isEmail().normalizeEmail()
], validate, UserController.updateUser)

/**
 * @swagger
 * /api/users/{id}:
 *   delete:
 *     tags: [Users]
 *     summary: Delete user (Admin only)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: User deleted successfully
 *       404:
 *         description: User not found
 */
router.delete('/:id', [
  auth,
  authorize('admin'),
  param('id').isMongoId().withMessage('Invalid user ID')
], validate, UserController.deleteUser)

export { router as userRoutes }`,
      'src/routes/products.ts': `import { Router } from 'express'
import { body, param, query } from 'express-validator'
import { ProductController } from '../controllers/ProductController'
import { validate } from '../middleware/validate'
import { auth, authorize } from '../middleware/auth'

const router = Router()

/**
 * @swagger
 * /api/products:
 *   get:
 *     tags: [Products]
 *     summary: Get all products
 *     parameters:
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           minimum: 1
 *         description: Page number
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           minimum: 1
 *           maximum: 100
 *         description: Number of items per page
 *       - in: query
 *         name: category
 *         schema:
 *           type: string
 *         description: Filter by category
 *       - in: query
 *         name: search
 *         schema:
 *           type: string
 *         description: Search in name and description
 *     responses:
 *       200:
 *         description: Products retrieved successfully
 */
router.get('/', [
  query('page').optional().isInt({ min: 1 }),
  query('limit').optional().isInt({ min: 1, max: 100 }),
  query('category').optional().isIn(['electronics', 'clothing', 'books', 'home', 'sports', 'other']),
  query('search').optional().isLength({ min: 1 })
], validate, ProductController.getProducts)

/**
 * @swagger
 * /api/products:
 *   post:
 *     tags: [Products]
 *     summary: Create a new product
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - name
 *               - description
 *               - price
 *               - category
 *             properties:
 *               name:
 *                 type: string
 *               description:
 *                 type: string
 *               price:
 *                 type: number
 *               category:
 *                 type: string
 *               stock:
 *                 type: integer
 *     responses:
 *       201:
 *         description: Product created successfully
 */
router.post('/', [
  auth,
  body('name').trim().isLength({ min: 1, max: 100 }).withMessage('Name must be between 1 and 100 characters'),
  body('description').trim().isLength({ min: 1, max: 500 }).withMessage('Description must be between 1 and 500 characters'),
  body('price').isFloat({ min: 0 }).withMessage('Price must be a positive number'),
  body('category').isIn(['electronics', 'clothing', 'books', 'home', 'sports', 'other']).withMessage('Invalid category'),
  body('stock').optional().isInt({ min: 0 }).withMessage('Stock must be a non-negative integer')
], validate, ProductController.createProduct)

router.get('/:id', [
  param('id').isMongoId().withMessage('Invalid product ID')
], validate, ProductController.getProductById)

router.put('/:id', [
  auth,
  param('id').isMongoId().withMessage('Invalid product ID'),
  body('name').optional().trim().isLength({ min: 1, max: 100 }),
  body('description').optional().trim().isLength({ min: 1, max: 500 }),
  body('price').optional().isFloat({ min: 0 }),
  body('category').optional().isIn(['electronics', 'clothing', 'books', 'home', 'sports', 'other']),
  body('stock').optional().isInt({ min: 0 })
], validate, ProductController.updateProduct)

router.delete('/:id', [
  auth,
  param('id').isMongoId().withMessage('Invalid product ID')
], validate, ProductController.deleteProduct)

export { router as productRoutes }`,
      'src/controllers/UserController.ts': `import { Request, Response } from 'express'
import { User } from '../models/User'
import { ApiError } from '../utils/ApiError'
import { catchAsync } from '../utils/catchAsync'

export class UserController {
  static getUsers = catchAsync(async (req: Request, res: Response) => {
    const page = parseInt(req.query.page as string) || 1
    const limit = parseInt(req.query.limit as string) || 10
    const skip = (page - 1) * limit

    const [users, total] = await Promise.all([
      User.find({ isActive: true })
        .select('-password')
        .skip(skip)
        .limit(limit)
        .sort({ createdAt: -1 }),
      User.countDocuments({ isActive: true })
    ])

    const totalPages = Math.ceil(total / limit)

    res.json({
      success: true,
      data: {
        users,
        pagination: {
          page,
          limit,
          total,
          totalPages,
          hasNext: page < totalPages,
          hasPrev: page > 1
        }
      }
    })
  })

  static getUserById = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params
    
    const user = await User.findById(id).select('-password')
    if (!user || !user.isActive) {
      throw new ApiError(404, 'User not found')
    }

    res.json({
      success: true,
      data: { user }
    })
  })

  static updateUser = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params
    const updates = req.body

    // Users can only update their own profile, unless they're admin
    if (req.user?.userId !== id && req.user?.role !== 'admin') {
      throw new ApiError(403, 'You can only update your own profile')
    }

    const user = await User.findByIdAndUpdate(
      id,
      { ...updates, updatedAt: new Date() },
      { new: true, runValidators: true }
    ).select('-password')

    if (!user) {
      throw new ApiError(404, 'User not found')
    }

    res.json({
      success: true,
      message: 'User updated successfully',
      data: { user }
    })
  })

  static deleteUser = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params

    const user = await User.findByIdAndUpdate(
      id,
      { isActive: false },
      { new: true }
    )

    if (!user) {
      throw new ApiError(404, 'User not found')
    }

    res.json({
      success: true,
      message: 'User deleted successfully'
    })
  })
}`,
      'src/controllers/ProductController.ts': `import { Request, Response } from 'express'
import { Product } from '../models/Product'
import { ApiError } from '../utils/ApiError'
import { catchAsync } from '../utils/catchAsync'

export class ProductController {
  static getProducts = catchAsync(async (req: Request, res: Response) => {
    const page = parseInt(req.query.page as string) || 1
    const limit = parseInt(req.query.limit as string) || 10
    const skip = (page - 1) * limit
    const category = req.query.category as string
    const search = req.query.search as string

    // Build query
    const query: any = { isActive: true }
    
    if (category) {
      query.category = category
    }
    
    if (search) {
      query.$text = { $search: search }
    }

    const [products, total] = await Promise.all([
      Product.find(query)
        .populate('createdBy', 'name email')
        .skip(skip)
        .limit(limit)
        .sort({ createdAt: -1 }),
      Product.countDocuments(query)
    ])

    const totalPages = Math.ceil(total / limit)

    res.json({
      success: true,
      data: {
        products,
        pagination: {
          page,
          limit,
          total,
          totalPages,
          hasNext: page < totalPages,
          hasPrev: page > 1
        }
      }
    })
  })

  static createProduct = catchAsync(async (req: Request, res: Response) => {
    const productData = {
      ...req.body,
      createdBy: req.user?.userId
    }

    const product = await Product.create(productData)
    await product.populate('createdBy', 'name email')

    res.status(201).json({
      success: true,
      message: 'Product created successfully',
      data: { product }
    })
  })

  static getProductById = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params
    
    const product = await Product.findById(id)
      .populate('createdBy', 'name email')
    
    if (!product || !product.isActive) {
      throw new ApiError(404, 'Product not found')
    }

    res.json({
      success: true,
      data: { product }
    })
  })

  static updateProduct = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params
    const updates = req.body

    const product = await Product.findById(id)
    if (!product || !product.isActive) {
      throw new ApiError(404, 'Product not found')
    }

    // Users can only update their own products, unless they're admin
    if (product.createdBy.toString() !== req.user?.userId && req.user?.role !== 'admin') {
      throw new ApiError(403, 'You can only update your own products')
    }

    const updatedProduct = await Product.findByIdAndUpdate(
      id,
      { ...updates, updatedAt: new Date() },
      { new: true, runValidators: true }
    ).populate('createdBy', 'name email')

    res.json({
      success: true,
      message: 'Product updated successfully',
      data: { product: updatedProduct }
    })
  })

  static deleteProduct = catchAsync(async (req: Request, res: Response) => {
    const { id } = req.params

    const product = await Product.findById(id)
    if (!product || !product.isActive) {
      throw new ApiError(404, 'Product not found')
    }

    // Users can only delete their own products, unless they're admin
    if (product.createdBy.toString() !== req.user?.userId && req.user?.role !== 'admin') {
      throw new ApiError(403, 'You can only delete your own products')
    }

    await Product.findByIdAndUpdate(id, { isActive: false })

    res.json({
      success: true,
      message: 'Product deleted successfully'
    })
  })
}`,
      '.env.example': `# Server Configuration
NODE_ENV=development
PORT=3000

# Database
MONGODB_URI=mongodb://localhost:27017/express-api

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRES_IN=7d

# CORS Configuration
FRONTEND_URL=http://localhost:3000

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100`,
      'tsconfig.json': `{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true,
    "removeComments": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "moduleResolution": "node",
    "allowSyntheticDefaultImports": true,
    "experimentalDecorators": true,
    "emitDecoratorMetadata": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist", "**/*.test.ts", "**/*.spec.ts"]
}`,
      'Dockerfile': `FROM node:18-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM node:18-alpine AS production

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nodeuser -u 1001

# Copy built application
COPY --from=builder --chown=nodeuser:nodejs /app/dist ./dist
COPY --from=builder --chown=nodeuser:nodejs /app/node_modules ./node_modules
COPY --from=builder --chown=nodeuser:nodejs /app/package*.json ./

# Switch to non-root user
USER nodeuser

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost:3000/health || exit 1

# Start the application
CMD ["npm", "start"]`,
      'docker-compose.yml': `version: '3.8'

services:
  api:
    build: .
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - MONGODB_URI=mongodb://mongo:27017/express-api
      - JWT_SECRET=your-super-secret-jwt-key
    depends_on:
      - mongo
    networks:
      - app-network
    restart: unless-stopped

  mongo:
    image: mongo:6.0
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
    networks:
      - app-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - app-network
    restart: unless-stopped

volumes:
  mongo-data:
  redis-data:

networks:
  app-network:
    driver: bridge`
    },
    dependencies: {
      'express': '^4.18.2',
      'cors': '^2.8.5',
      'helmet': '^7.1.0',
      'morgan': '^1.10.0',
      'mongoose': '^8.0.3',
      'bcryptjs': '^2.4.3',
      'jsonwebtoken': '^9.0.2',
      'express-validator': '^7.0.1',
      'express-rate-limit': '^7.1.5',
      'swagger-ui-express': '^5.0.0',
      'swagger-jsdoc': '^6.2.8',
      'dotenv': '^16.3.1'
    },
    devDependencies: {
      'typescript': '^5.3.3',
      '@types/express': '^4.17.21',
      '@types/cors': '^2.8.17',
      '@types/bcryptjs': '^2.4.6',
      '@types/jsonwebtoken': '^9.0.5',
      '@types/morgan': '^1.9.9',
      '@types/swagger-ui-express': '^4.1.6',
      '@types/swagger-jsdoc': '^6.0.4',
      'nodemon': '^3.0.2',
      'ts-node': '^10.9.2',
      'jest': '^29.7.0',
      '@types/jest': '^29.5.8',
      'supertest': '^6.3.3',
      '@types/supertest': '^6.0.2'
    },
    scripts: {
      'dev': 'nodemon src/server.ts',
      'build': 'tsc',
      'start': 'node dist/server.js',
      'test': 'jest',
      'test:watch': 'jest --watch',
      'docker:build': 'docker build -t express-api .',
      'docker:run': 'docker-compose up -d'
    },
    category: 'backend',
    tags: ['express', 'mongodb', 'typescript', 'jwt', 'rest', 'production']
  },
  {
    id: 'express-postgresql',
    name: 'Express + PostgreSQL',
    description: 'Enterprise-grade Express API with PostgreSQL, Prisma ORM, and advanced features',
    framework: 'express',
    features: ['TypeScript', 'PostgreSQL', 'Prisma ORM', 'JWT Auth', 'Redis Caching', 'GraphQL', 'WebSockets'],
    files: {
      'src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import compression from 'compression'
import rateLimit from 'express-rate-limit'
import { createServer } from 'http'
import { Server } from 'socket.io'
import { PrismaClient } from '@prisma/client'
import { errorHandler, notFound } from './middleware/errorHandler'
import { authRoutes } from './routes/auth'
import { userRoutes } from './routes/users'
import { productRoutes } from './routes/products'
import { graphqlServer } from './graphql/server'
import { setupWebSockets } from './websockets/setup'
import swaggerUi from 'swagger-ui-express'
import { swaggerSpec } from './config/swagger'
import { redisClient } from './config/redis'

const app = express()
const server = createServer(app)
const io = new Server(server, {
  cors: {
    origin: process.env.FRONTEND_URL || "http://localhost:3000",
    credentials: true
  }
})

export const prisma = new PrismaClient({
  log: process.env.NODE_ENV === 'development' ? ['query', 'error', 'warn'] : ['error'],
})

const PORT = process.env.PORT || 3000

// Compression middleware
app.use(compression())

// Security middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
}))

// Rate limiting with Redis store
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: 'Too many requests from this IP, please try again later.',
  standardHeaders: true,
  legacyHeaders: false,
})
app.use('/api/', limiter)

// CORS configuration
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:3000',
  credentials: true
}))

// Body parsing middleware
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

// Logging
app.use(morgan('combined'))

// GraphQL endpoint
app.use('/graphql', graphqlServer)

// API documentation
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec))

// REST API routes
app.use('/api/auth', authRoutes)
app.use('/api/users', userRoutes)
app.use('/api/products', productRoutes)

// Health check with database connectivity
app.get('/health', async (req, res) => {
  try {
    await prisma.$queryRaw\`SELECT 1\`
    const redisStatus = redisClient.status
    
    res.json({ 
      status: 'OK', 
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: process.env.NODE_ENV || 'development',
      database: 'connected',
      redis: redisStatus,
      memory: process.memoryUsage()
    })
  } catch (error) {
    res.status(503).json({
      status: 'ERROR',
      timestamp: new Date().toISOString(),
      database: 'disconnected',
      error: 'Database connection failed'
    })
  }
})

// WebSocket setup
setupWebSockets(io)

// Error handling
app.use(notFound)
app.use(errorHandler)

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('SIGTERM received, shutting down gracefully')
  server.close(() => {
    console.log('HTTP server closed')
  })
  
  await prisma.$disconnect()
  await redisClient.disconnect()
  process.exit(0)
})

const startServer = async () => {
  try {
    // Connect to Redis
    await redisClient.connect()
    console.log('✅ Redis connected successfully')
    
    // Test database connection
    await prisma.$connect()
    console.log('✅ Database connected successfully')
    
    server.listen(PORT, () => {
      console.log(\`🚀 Server running on port \${PORT}\`)
      console.log(\`📚 API Documentation: http://localhost:\${PORT}/api-docs\`)
      console.log(\`🏥 Health Check: http://localhost:\${PORT}/health\`)
      console.log(\`🔄 GraphQL Playground: http://localhost:\${PORT}/graphql\`)
    })
  } catch (error) {
    console.error('Failed to start server:', error)
    process.exit(1)
  }
}

startServer()
`,
      'prisma/schema.prisma': `generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

model User {
  id        String   @id @default(cuid())
  email     String   @unique
  name      String
  password  String
  role      Role     @default(USER)
  isActive  Boolean  @default(true)
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  // Relations
  products Product[]
  orders   Order[]
  reviews  Review[]

  @@map("users")
}

model Product {
  id          String   @id @default(cuid())
  name        String
  description String
  price       Decimal  @db.Decimal(10, 2)
  category    Category
  stock       Int      @default(0)
  isActive    Boolean  @default(true)
  images      String[]
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  // Relations
  createdBy   String
  creator     User        @relation(fields: [createdBy], references: [id])
  orderItems  OrderItem[]
  reviews     Review[]

  @@map("products")
}

model Order {
  id        String      @id @default(cuid())
  total     Decimal     @db.Decimal(10, 2)
  status    OrderStatus @default(PENDING)
  createdAt DateTime    @default(now())
  updatedAt DateTime    @updatedAt

  // Relations
  userId String
  user   User        @relation(fields: [userId], references: [id])
  items  OrderItem[]

  @@map("orders")
}

model OrderItem {
  id       String  @id @default(cuid())
  quantity Int
  price    Decimal @db.Decimal(10, 2)

  // Relations
  orderId   String
  order     Order   @relation(fields: [orderId], references: [id])
  productId String
  product   Product @relation(fields: [productId], references: [id])

  @@unique([orderId, productId])
  @@map("order_items")
}

model Review {
  id        String   @id @default(cuid())
  rating    Int
  comment   String?
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  // Relations
  userId    String
  user      User    @relation(fields: [userId], references: [id])
  productId String
  product   Product @relation(fields: [productId], references: [id])

  @@unique([userId, productId])
  @@map("reviews")
}

enum Role {
  USER
  ADMIN
}

enum Category {
  ELECTRONICS
  CLOTHING
  BOOKS
  HOME
  SPORTS
  OTHER
}

enum OrderStatus {
  PENDING
  PROCESSING
  SHIPPED
  DELIVERED
  CANCELLED
}`,
      'src/config/redis.ts': `import { createClient } from 'redis'

export const redisClient = createClient({
  url: process.env.REDIS_URL || 'redis://localhost:6379',
  socket: {
    reconnectStrategy: (retries) => Math.min(retries * 50, 1000)
  }
})

redisClient.on('error', (err) => {
  console.error('Redis error:', err)
})

redisClient.on('connect', () => {
  console.log('✅ Redis connected')
})

redisClient.on('disconnect', () => {
  console.log('📡 Redis disconnected')
})

export const cacheService = {
  async get<T>(key: string): Promise<T | null> {
    try {
      const value = await redisClient.get(key)
      return value ? JSON.parse(value) : null
    } catch (error) {
      console.error(\`Cache get error for key \${key}:\`, error)
      return null
    }
  },

  async set(key: string, value: any, ttl?: number): Promise<void> {
    try {
      const serialized = JSON.stringify(value)
      if (ttl) {
        await redisClient.setEx(key, ttl, serialized)
      } else {
        await redisClient.set(key, serialized)
      }
    } catch (error) {
      console.error(\`Cache set error for key \${key}:\`, error)
    }
  },

  async del(key: string): Promise<void> {
    try {
      await redisClient.del(key)
    } catch (error) {
      console.error(\`Cache delete error for key \${key}:\`, error)
    }
  },

  async exists(key: string): Promise<boolean> {
    try {
      const result = await redisClient.exists(key)
      return result === 1
    } catch (error) {
      console.error(\`Cache exists error for key \${key}:\`, error)
      return false
    }
  }
}`,
      'src/websockets/setup.ts': `import { Server } from 'socket.io'
import jwt from 'jsonwebtoken'
import { prisma } from '../server'

interface AuthenticatedSocket {
  userId?: string
  role?: string
}

export const setupWebSockets = (io: Server) => {
  // Authentication middleware for WebSocket connections
  io.use(async (socket: any, next) => {
    try {
      const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace('Bearer ', '')
      
      if (!token) {
        return next(new Error('Authentication required'))
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key') as any
      const user = await prisma.user.findUnique({
        where: { id: decoded.userId, isActive: true }
      })

      if (!user) {
        return next(new Error('User not found'))
      }

      socket.userId = user.id
      socket.role = user.role
      next()
    } catch (error) {
      next(new Error('Invalid token'))
    }
  })

  io.on('connection', (socket: any) => {
    console.log(\`User \${socket.userId} connected via WebSocket\`)
    
    // Join user-specific room
    socket.join(\`user:\${socket.userId}\`)
    
    // Join role-based rooms
    if (socket.role === 'ADMIN') {
      socket.join('admins')
    }

    // Handle real-time notifications
    socket.on('subscribe:notifications', () => {
      socket.join(\`notifications:\${socket.userId}\`)
      console.log(\`User \${socket.userId} subscribed to notifications\`)
    })

    // Handle real-time product updates
    socket.on('subscribe:products', () => {
      socket.join('product-updates')
      console.log(\`User \${socket.userId} subscribed to product updates\`)
    })

    // Handle chat functionality
    socket.on('join:room', (roomId: string) => {
      socket.join(\`room:\${roomId}\`)
      socket.to(\`room:\${roomId}\`).emit('user:joined', {
        userId: socket.userId,
        timestamp: new Date()
      })
    })

    socket.on('send:message', (data: { roomId: string, message: string }) => {
      socket.to(\`room:\${data.roomId}\`).emit('new:message', {
        userId: socket.userId,
        message: data.message,
        timestamp: new Date()
      })
    })

    socket.on('disconnect', () => {
      console.log(\`User \${socket.userId} disconnected\`)
    })
  })

  return io
}

// Utility functions for sending real-time updates
export const notifyUser = (io: Server, userId: string, event: string, data: any) => {
  io.to(\`user:\${userId}\`).emit(event, data)
}

export const notifyAdmins = (io: Server, event: string, data: any) => {
  io.to('admins').emit(event, data)
}

export const broadcastProductUpdate = (io: Server, productData: any) => {
  io.to('product-updates').emit('product:updated', productData)
}`,
      'package.json': `{
  "name": "express-postgresql-api",
  "version": "1.0.0",
  "description": "Production-ready Express API with PostgreSQL and advanced features",
  "main": "dist/server.js",
  "scripts": {
    "dev": "nodemon src/server.ts",
    "build": "prisma generate && tsc",
    "start": "node dist/server.js",
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "db:migrate": "prisma migrate dev",
    "db:deploy": "prisma migrate deploy",
    "db:seed": "ts-node prisma/seed.ts",
    "db:studio": "prisma studio",
    "lint": "eslint src/**/*.ts",
    "lint:fix": "eslint src/**/*.ts --fix",
    "docker:build": "docker build -t express-postgresql-api .",
    "docker:run": "docker-compose up -d"
  },
  "keywords": ["express", "postgresql", "prisma", "typescript", "api"],
  "author": "Your Name",
  "license": "MIT"
}`
    },
    dependencies: {
      'express': '^4.18.2',
      'cors': '^2.8.5',
      'helmet': '^7.1.0',
      'morgan': '^1.10.0',
      'compression': '^1.7.4',
      '@prisma/client': '^5.7.1',
      'prisma': '^5.7.1',
      'bcryptjs': '^2.4.3',
      'jsonwebtoken': '^9.0.2',
      'express-validator': '^7.0.1',
      'express-rate-limit': '^7.1.5',
      'swagger-ui-express': '^5.0.0',
      'swagger-jsdoc': '^6.2.8',
      'apollo-server-express': '^3.12.1',
      'graphql': '^16.8.1',
      'socket.io': '^4.7.4',
      'redis': '^4.6.11',
      'dotenv': '^16.3.1',
      'joi': '^17.11.0',
      'multer': '^1.4.5-lts.1',
      'sharp': '^0.33.1'
    },
    devDependencies: {
      'typescript': '^5.3.3',
      '@types/express': '^4.17.21',
      '@types/cors': '^2.8.17',
      '@types/bcryptjs': '^2.4.6',
      '@types/jsonwebtoken': '^9.0.5',
      '@types/morgan': '^1.9.9',
      '@types/compression': '^1.7.5',
      '@types/swagger-ui-express': '^4.1.6',
      '@types/swagger-jsdoc': '^6.0.4',
      '@types/multer': '^1.4.11',
      'nodemon': '^3.0.2',
      'ts-node': '^10.9.2',
      'jest': '^29.7.0',
      '@types/jest': '^29.5.8',
      'supertest': '^6.3.3',
      '@types/supertest': '^6.0.2',
      'eslint': '^8.55.0',
      '@typescript-eslint/eslint-plugin': '^6.13.2',
      '@typescript-eslint/parser': '^6.13.2'
    },
    scripts: {
      'dev': 'nodemon src/server.ts',
      'build': 'prisma generate && tsc',
      'start': 'node dist/server.js',
      'test': 'jest',
      'test:watch': 'jest --watch',
      'db:migrate': 'prisma migrate dev',
      'db:deploy': 'prisma migrate deploy',
      'db:seed': 'ts-node prisma/seed.ts',
      'docker:build': 'docker build -t express-postgresql-api .',
      'docker:run': 'docker-compose up -d'
    },
    category: 'backend',
    tags: ['express', 'postgresql', 'prisma', 'typescript', 'graphql', 'websockets', 'redis', 'production']
  },
  {
    id: 'express-microservices',
    name: 'Express Microservices',
    description: 'Microservices architecture with Express, Docker, and service discovery',
    framework: 'express',
    features: ['Microservices', 'Docker', 'Service Discovery', 'API Gateway', 'Message Queue', 'Monitoring'],
    files: {
      'services/api-gateway/src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import httpProxy from 'http-proxy-middleware'
import rateLimit from 'express-rate-limit'
import { serviceRegistry } from './serviceRegistry'
import { authMiddleware } from './middleware/auth'
import { requestLogger } from './middleware/requestLogger'

const app = express()
const PORT = process.env.PORT || 3000

// Security middleware
app.use(helmet())
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:3000',
  credentials: true
}))

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 1000,
  message: 'Too many requests from this IP'
})
app.use(limiter)

// Body parsing
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true }))
app.use(morgan('combined'))
app.use(requestLogger)

// Service discovery and routing
app.use('/api/auth', authMiddleware, httpProxy({
  target: () => serviceRegistry.getService('auth-service'),
  changeOrigin: true,
  pathRewrite: { '^/api/auth': '' },
  onError: (err, req, res) => {
    console.error('Auth service error:', err)
    res.status(503).json({ error: 'Auth service unavailable' })
  }
}))

app.use('/api/users', authMiddleware, httpProxy({
  target: () => serviceRegistry.getService('user-service'),
  changeOrigin: true,
  pathRewrite: { '^/api/users': '' },
  onError: (err, req, res) => {
    res.status(503).json({ error: 'User service unavailable' })
  }
}))

app.use('/api/products', httpProxy({
  target: () => serviceRegistry.getService('product-service'),
  changeOrigin: true,
  pathRewrite: { '^/api/products': '' },
  onError: (err, req, res) => {
    res.status(503).json({ error: 'Product service unavailable' })
  }
}))

app.use('/api/orders', authMiddleware, httpProxy({
  target: () => serviceRegistry.getService('order-service'),
  changeOrigin: true,
  pathRewrite: { '^/api/orders': '' },
  onError: (err, req, res) => {
    res.status(503).json({ error: 'Order service unavailable' })
  }
}))

// Health check
app.get('/health', (req, res) => {
  const services = serviceRegistry.getAllServices()
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    services: Object.keys(services),
    gateway: 'api-gateway',
    uptime: process.uptime()
  })
})

// Service discovery endpoint
app.get('/services', (req, res) => {
  res.json(serviceRegistry.getAllServices())
})

app.listen(PORT, () => {
  console.log(\`🚀 API Gateway running on port \${PORT}\`)
  serviceRegistry.startDiscovery()
})`,
      'services/auth-service/src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import mongoose from 'mongoose'
import { authRoutes } from './routes/auth'
import { errorHandler } from './middleware/errorHandler'
import { serviceRegistry } from './utils/serviceRegistry'

const app = express()
const PORT = process.env.PORT || 3001
const SERVICE_NAME = 'auth-service'

app.use(helmet())
app.use(cors())
app.use(morgan('combined'))
app.use(express.json())

app.use('/auth', authRoutes)

app.get('/health', (req, res) => {
  res.json({
    service: SERVICE_NAME,
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  })
})

app.use(errorHandler)

const startService = async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://mongo:27017/auth-db')
    console.log('✅ Auth service database connected')
    
    app.listen(PORT, () => {
      console.log(\`🔐 Auth Service running on port \${PORT}\`)
      
      // Register with service discovery
      serviceRegistry.register(SERVICE_NAME, \`http://\${SERVICE_NAME}:\${PORT}\`)
    })
  } catch (error) {
    console.error('Failed to start auth service:', error)
    process.exit(1)
  }
}

startService()`,
      'services/user-service/src/server.ts': `import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import morgan from 'morgan'
import { PrismaClient } from '@prisma/client'
import { userRoutes } from './routes/users'
import { errorHandler } from './middleware/errorHandler'
import { serviceRegistry } from './utils/serviceRegistry'

const app = express()
const prisma = new PrismaClient()
const PORT = process.env.PORT || 3002
const SERVICE_NAME = 'user-service'

app.use(helmet())
app.use(cors())
app.use(morgan('combined'))
app.use(express.json())

app.use('/users', userRoutes)

app.get('/health', async (req, res) => {
  try {
    await prisma.$queryRaw\`SELECT 1\`
    res.json({
      service: SERVICE_NAME,
      status: 'OK',
      database: 'connected',
      timestamp: new Date().toISOString(),
      uptime: process.uptime()
    })
  } catch (error) {
    res.status(503).json({
      service: SERVICE_NAME,
      status: 'ERROR',
      database: 'disconnected',
      timestamp: new Date().toISOString()
    })
  }
})

app.use(errorHandler)

const startService = async () => {
  try {
    await prisma.$connect()
    console.log('✅ User service database connected')
    
    app.listen(PORT, () => {
      console.log(\`👥 User Service running on port \${PORT}\`)
      
      // Register with service discovery
      serviceRegistry.register(SERVICE_NAME, \`http://\${SERVICE_NAME}:\${PORT}\`)
    })
  } catch (error) {
    console.error('Failed to start user service:', error)
    process.exit(1)
  }
}

startService()`,
      'docker-compose.microservices.yml': `version: '3.8'

services:
  # API Gateway
  api-gateway:
    build: ./services/api-gateway
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - FRONTEND_URL=http://localhost:3000
      - CONSUL_HOST=consul
    depends_on:
      - consul
      - redis
    networks:
      - microservices
    restart: unless-stopped

  # Auth Service
  auth-service:
    build: ./services/auth-service
    environment:
      - NODE_ENV=production
      - MONGODB_URI=mongodb://mongo-auth:27017/auth-db
      - JWT_SECRET=auth-service-secret
      - CONSUL_HOST=consul
    depends_on:
      - mongo-auth
      - consul
    networks:
      - microservices
    restart: unless-stopped

  # User Service
  user-service:
    build: ./services/user-service
    environment:
      - NODE_ENV=production
      - DATABASE_URL=postgresql://userdb:password@postgres-users:5432/users_db
      - CONSUL_HOST=consul
    depends_on:
      - postgres-users
      - consul
    networks:
      - microservices
    restart: unless-stopped

  # Product Service
  product-service:
    build: ./services/product-service
    environment:
      - NODE_ENV=production
      - MONGODB_URI=mongodb://mongo-products:27017/products-db
      - CONSUL_HOST=consul
    depends_on:
      - mongo-products
      - consul
    networks:
      - microservices
    restart: unless-stopped

  # Order Service
  order-service:
    build: ./services/order-service
    environment:
      - NODE_ENV=production
      - DATABASE_URL=postgresql://orderdb:password@postgres-orders:5432/orders_db
      - CONSUL_HOST=consul
      - RABBITMQ_URL=amqp://rabbitmq:5672
    depends_on:
      - postgres-orders
      - consul
      - rabbitmq
    networks:
      - microservices
    restart: unless-stopped

  # Databases
  mongo-auth:
    image: mongo:6.0
    volumes:
      - mongo-auth-data:/data/db
    networks:
      - microservices
    restart: unless-stopped

  mongo-products:
    image: mongo:6.0
    volumes:
      - mongo-products-data:/data/db
    networks:
      - microservices
    restart: unless-stopped

  postgres-users:
    image: postgres:15
    environment:
      - POSTGRES_DB=users_db
      - POSTGRES_USER=userdb
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres-users-data:/var/lib/postgresql/data
    networks:
      - microservices
    restart: unless-stopped

  postgres-orders:
    image: postgres:15
    environment:
      - POSTGRES_DB=orders_db
      - POSTGRES_USER=orderdb
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres-orders-data:/var/lib/postgresql/data
    networks:
      - microservices
    restart: unless-stopped

  # Service Discovery
  consul:
    image: consul:1.15
    ports:
      - "8500:8500"
    command: agent -server -bootstrap-expect=1 -ui -node=consul-server -client=0.0.0.0
    networks:
      - microservices
    restart: unless-stopped

  # Message Queue
  rabbitmq:
    image: rabbitmq:3.11-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=password
    networks:
      - microservices
    restart: unless-stopped

  # Cache
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - microservices
    restart: unless-stopped

  # Monitoring
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - microservices
    restart: unless-stopped

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - microservices
    restart: unless-stopped

volumes:
  mongo-auth-data:
  mongo-products-data:
  postgres-users-data:
  postgres-orders-data:
  redis-data:
  grafana-data:

networks:
  microservices:
    driver: bridge`,
      'services/api-gateway/src/serviceRegistry.ts': `import consul from 'consul'

class ServiceRegistry {
  private consul: consul.Consul
  private services: Map<string, string> = new Map()

  constructor() {
    this.consul = consul({
      host: process.env.CONSUL_HOST || 'localhost',
      port: process.env.CONSUL_PORT || '8500'
    })
  }

  async register(serviceName: string, serviceUrl: string): Promise<void> {
    const serviceId = \`\${serviceName}-\${Date.now()}\`
    
    try {
      await this.consul.agent.service.register({
        id: serviceId,
        name: serviceName,
        address: serviceUrl.split('://')[1].split(':')[0],
        port: parseInt(serviceUrl.split(':').pop() || '3000'),
        check: {
          http: \`\${serviceUrl}/health\`,
          interval: '10s',
          timeout: '5s'
        }
      })
      
      this.services.set(serviceName, serviceUrl)
      console.log(\`✅ Service \${serviceName} registered at \${serviceUrl}\`)
    } catch (error) {
      console.error(\`❌ Failed to register service \${serviceName}:\`, error)
    }
  }

  async deregister(serviceId: string): Promise<void> {
    try {
      await this.consul.agent.service.deregister(serviceId)
      console.log(\`Service \${serviceId} deregistered\`)
    } catch (error) {
      console.error(\`Failed to deregister service \${serviceId}:\`, error)
    }
  }

  async getService(serviceName: string): Promise<string> {
    try {
      const services = await this.consul.health.service({
        service: serviceName,
        passing: true
      })
      
      if (services.length === 0) {
        throw new Error(\`No healthy instances of \${serviceName} found\`)
      }
      
      // Simple round-robin load balancing
      const service = services[Math.floor(Math.random() * services.length)]
      return \`http://\${service.Service.Address}:\${service.Service.Port}\`
    } catch (error) {
      console.error(\`Failed to get service \${serviceName}:\`, error)
      // Fallback to cached service if available
      return this.services.get(serviceName) || \`http://\${serviceName}:3000\`
    }
  }

  getAllServices(): Record<string, string> {
    return Object.fromEntries(this.services)
  }

  startDiscovery(): void {
    setInterval(async () => {
      try {
        const services = await this.consul.agent.service.list()
        this.services.clear()
        
        Object.values(services).forEach((service: any) => {
          const serviceUrl = \`http://\${service.Address}:\${service.Port}\`
          this.services.set(service.Service, serviceUrl)
        })
      } catch (error) {
        console.error('Service discovery error:', error)
      }
    }, 30000) // Check every 30 seconds
  }
}

export const serviceRegistry = new ServiceRegistry()`,
      'monitoring/prometheus.yml': `global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:3000']
    metrics_path: '/metrics'
    scrape_interval: 5s

  - job_name: 'auth-service'
    static_configs:
      - targets: ['auth-service:3001']
    metrics_path: '/metrics'
    scrape_interval: 5s

  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:3002']
    metrics_path: '/metrics'
    scrape_interval: 5s

  - job_name: 'product-service'
    static_configs:
      - targets: ['product-service:3003']
    metrics_path: '/metrics'
    scrape_interval: 5s

  - job_name: 'order-service'
    static_configs:
      - targets: ['order-service:3004']
    metrics_path: '/metrics'
    scrape_interval: 5s`,
      'README.md': `# Express Microservices Architecture

A production-ready microservices architecture built with Express.js, featuring service discovery, API gateway, and comprehensive monitoring.

## Services

- **API Gateway** (Port 3000): Routes requests to appropriate services
- **Auth Service** (Port 3001): Handles authentication and authorization
- **User Service** (Port 3002): Manages user data and profiles
- **Product Service** (Port 3003): Manages product catalog
- **Order Service** (Port 3004): Handles order processing

## Infrastructure

- **Consul**: Service discovery and configuration
- **RabbitMQ**: Message queue for async communication
- **Redis**: Caching and session storage
- **PostgreSQL**: Relational database for users and orders
- **MongoDB**: Document database for auth and products
- **Prometheus**: Metrics collection
- **Grafana**: Monitoring dashboards

## Quick Start

1. Clone the repository
2. Run \`docker-compose -f docker-compose.microservices.yml up -d\`
3. Access the API Gateway at http://localhost:3000
4. Monitor services at http://localhost:8500 (Consul)
5. View metrics at http://localhost:3001 (Grafana)

## API Endpoints

- \`POST /api/auth/login\` - User authentication
- \`GET /api/users\` - List users
- \`GET /api/products\` - List products
- \`POST /api/orders\` - Create order

## Development

Each service can be developed independently with its own database and dependencies.`
    },
    dependencies: {
      'express': '^4.18.2',
      'cors': '^2.8.5',
      'helmet': '^7.1.0',
      'morgan': '^1.10.0',
      'http-proxy-middleware': '^3.0.0',
      'express-rate-limit': '^7.1.5',
      'consul': '^0.40.0',
      'amqplib': '^0.10.3',
      'prom-client': '^15.1.0'
    },
    devDependencies: {
      'typescript': '^5.3.3',
      '@types/express': '^4.17.21',
      '@types/cors': '^2.8.17',
      'nodemon': '^3.0.2',
      'ts-node': '^10.9.2',
      'docker-compose': '^0.24.6'
    },
    scripts: {
      'dev:gateway': 'cd services/api-gateway && npm run dev',
      'dev:auth': 'cd services/auth-service && npm run dev',
      'dev:users': 'cd services/user-service && npm run dev',
      'build': 'npm run build --workspaces',
      'start': 'docker-compose -f docker-compose.microservices.yml up -d',
      'stop': 'docker-compose -f docker-compose.microservices.yml down',
      'logs': 'docker-compose -f docker-compose.microservices.yml logs -f',
      'test': 'npm test --workspaces'
    },
    category: 'backend',
    tags: ['microservices', 'express', 'docker', 'consul', 'api-gateway', 'production']
  },
  {
    id: 'android-kotlin',
    name: 'Android Kotlin',
    description: 'Native Android application with Kotlin and Jetpack Compose',
    framework: 'android',
    features: ['Kotlin', 'Jetpack Compose', 'Navigation', 'Material Design'],
    files: {
      'app/src/main/java/com/example/app/MainActivity.kt': `package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        Greeting("Android")
    }
}`,
    },
    dependencies: {},
    devDependencies: {},
    scripts: {
      'build': './gradlew build',
      'test': './gradlew test'
    },
    category: 'mobile',
    tags: ['android', 'kotlin', 'jetpack-compose', 'mobile']
  }
]

export function getTemplateById(id: string): FrameworkTemplate | undefined {
  return frameworkTemplates.find(template => template.id === id)
}

export function getTemplatesByFramework(framework: string): FrameworkTemplate[] {
  return frameworkTemplates.filter(template => template.framework === framework)
}

export function getTemplatesByCategory(category: string): FrameworkTemplate[] {
  return frameworkTemplates.filter(template => template.category === category)
}