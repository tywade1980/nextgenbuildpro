/**
 * Invoice Data Models for Construction BMS
 * 
 * This file defines the TypeScript interfaces for invoices, payments,
 * and related entities in the construction management system.
 */

/**
 * Invoice Status Enum
 */
export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  VIEWED = 'VIEWED',
  PARTIAL_PAID = 'PARTIAL_PAID',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

/**
 * Invoice Type Enum
 */
export enum InvoiceType {
  STANDARD = 'STANDARD',           // Regular invoice
  PROGRESS = 'PROGRESS',           // Progress billing invoice
  FINAL = 'FINAL',                 // Final invoice
  CHANGE_ORDER = 'CHANGE_ORDER',   // Change order invoice
  RETAINER = 'RETAINER',           // Retainer/deposit invoice
  CREDIT_NOTE = 'CREDIT_NOTE'      // Credit note/refund
}

/**
 * Payment Method Enum
 */
export enum PaymentMethod {
  CASH = 'CASH',
  CHECK = 'CHECK',
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  ACH = 'ACH',
  WIRE_TRANSFER = 'WIRE_TRANSFER',
  PAYPAL = 'PAYPAL',
  VENMO = 'VENMO',
  ZELLE = 'ZELLE',
  OTHER = 'OTHER'
}

/**
 * Payment Status Enum
 */
export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  CANCELLED = 'CANCELLED'
}

/**
 * Invoice Line Item
 */
export interface InvoiceLineItem {
  id: string;
  invoiceId: string;
  name: string;
  description: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  type: 'LABOR' | 'MATERIAL' | 'EQUIPMENT' | 'OTHER';
  categoryId?: string;
  categoryName?: string;
  
  // Cost breakdown
  materialCost: number;
  laborCost: number;
  equipmentCost: number;
  
  // Calculations
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  total: number;
  
  // Metadata
  assemblyId?: string;
  estimateItemId?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Invoice
 */
export interface Invoice {
  id: string;
  invoiceNumber: string;           // Auto-generated invoice number (e.g., INV-2024-001)
  
  // Relationships
  projectId?: string;
  projectName?: string;
  estimateId?: string;
  clientId: string;
  clientName: string;
  
  // Invoice details
  title: string;
  description?: string;
  type: InvoiceType;
  status: InvoiceStatus;
  
  // Dates
  issueDate: string;
  dueDate: string;
  paidDate?: string;
  
  // Line items
  items: InvoiceLineItem[];
  
  // Financial calculations
  subtotal: number;
  taxRate: number;
  taxAmount: number;
  discountRate: number;
  discountAmount: number;
  total: number;
  
  // Payment tracking
  amountPaid: number;
  amountDue: number;
  
  // Progress billing (for progress invoices)
  progressPercentage?: number;
  previouslyInvoiced?: number;
  
  // Terms and notes
  paymentTerms: string;
  notes?: string;
  termsAndConditions?: string;
  
  // Metadata
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  sentAt?: string;
  viewedAt?: string;
  
  // Custom fields
  customFields?: Record<string, string | number | boolean | null>;
}

/**
 * Payment Record
 */
export interface Payment {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  
  // Payment details
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  
  // Transaction details
  transactionId?: string;
  referenceNumber?: string;
  checkNumber?: string;
  
  // Dates
  paymentDate: string;
  processedDate?: string;
  
  // Notes
  notes?: string;
  
  // Metadata
  recordedBy: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Change Order
 */
export interface ChangeOrder {
  id: string;
  changeOrderNumber: string;        // Auto-generated (e.g., CO-2024-001)
  
  // Relationships
  projectId: string;
  projectName: string;
  originalEstimateId?: string;
  
  // Change order details
  title: string;
  description: string;
  reason: string;
  status: 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  
  // Financial impact
  originalAmount: number;
  changeAmount: number;
  newAmount: number;
  
  // Line items
  items: InvoiceLineItem[];
  
  // Approval
  requestedBy: string;
  approvedBy?: string;
  approvalDate?: string;
  
  // Dates
  requestDate: string;
  effectiveDate?: string;
  
  // Associated invoice
  invoiceId?: string;
  
  // Metadata
  createdAt: string;
  updatedAt: string;
  
  // Notes
  notes?: string;
}

/**
 * Invoice Summary (for dashboards and reports)
 */
export interface InvoiceSummary {
  totalInvoices: number;
  totalAmount: number;
  totalPaid: number;
  totalOutstanding: number;
  totalOverdue: number;
  
  // By status
  draftCount: number;
  sentCount: number;
  paidCount: number;
  overdueCount: number;
  
  // By type
  standardCount: number;
  progressCount: number;
  finalCount: number;
  changeOrderCount: number;
}

/**
 * Payment Summary (for dashboards and reports)
 */
export interface PaymentSummary {
  totalPayments: number;
  totalAmount: number;
  
  // By method
  cashAmount: number;
  checkAmount: number;
  creditCardAmount: number;
  achAmount: number;
  otherAmount: number;
  
  // By status
  completedCount: number;
  pendingCount: number;
  failedCount: number;
}

/**
 * Invoice Creation Request
 */
export interface CreateInvoiceRequest {
  projectId?: string;
  estimateId?: string;
  clientId: string;
  title: string;
  description?: string;
  type: InvoiceType;
  issueDate: string;
  dueDate: string;
  items: Omit<InvoiceLineItem, 'id' | 'invoiceId' | 'createdAt' | 'updatedAt'>[];
  taxRate: number;
  discountRate?: number;
  paymentTerms: string;
  notes?: string;
  termsAndConditions?: string;
  progressPercentage?: number;
}

/**
 * Payment Creation Request
 */
export interface CreatePaymentRequest {
  invoiceId: string;
  amount: number;
  method: PaymentMethod;
  paymentDate: string;
  transactionId?: string;
  referenceNumber?: string;
  checkNumber?: string;
  notes?: string;
}

/**
 * Estimate to Invoice Conversion Options
 */
export interface EstimateToInvoiceOptions {
  estimateId: string;
  invoiceType: InvoiceType;
  dueDate: string;
  paymentTerms: string;
  includeAllItems?: boolean;
  selectedItemIds?: string[];
  progressPercentage?: number;
  notes?: string;
}

/**
 * Invoice Filter Options
 */
export interface InvoiceFilterOptions {
  status?: InvoiceStatus[];
  type?: InvoiceType[];
  clientId?: string;
  projectId?: string;
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
  searchQuery?: string;
}

/**
 * Invoice Calculation Result
 */
export interface InvoiceCalculation {
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  total: number;
  amountDue: number;
  
  // Breakdown by type
  laborTotal: number;
  materialTotal: number;
  equipmentTotal: number;
  otherTotal: number;
}

export default {
  InvoiceStatus,
  InvoiceType,
  PaymentMethod,
  PaymentStatus
};
