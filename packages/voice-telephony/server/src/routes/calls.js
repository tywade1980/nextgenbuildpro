/**
 * Calls API Routes
 * Handles call management and history
 */

const express = require('express');
const router = express.Router();
const logger = require('../utils/logger');
const { authenticateToken } = require('./auth');
const { runQuery, getRow, getAllRows } = require('../database/init');
const { CallSession } = require('../models/CallSession');

/**
 * Get call history
 * GET /api/calls/history
 */
router.get('/history', authenticateToken, async (req, res) => {
  try {
    const { page = 1, limit = 50, status, direction, startDate, endDate } = req.query;
    const offset = (page - 1) * limit;

    // Build WHERE clause
    let whereClause = 'WHERE user_id = ?';
    const params = [req.user.userId];

    if (status) {
      whereClause += ' AND status = ?';
      params.push(status);
    }

    if (direction) {
      whereClause += ' AND direction = ?';
      params.push(direction);
    }

    if (startDate) {
      whereClause += ' AND start_time >= ?';
      params.push(startDate);
    }

    if (endDate) {
      whereClause += ' AND start_time <= ?';
      params.push(endDate);
    }

    // Get total count
    const countResult = await getRow(
      `SELECT COUNT(*) as total FROM call_sessions ${whereClause}`,
      params
    );

    // Get calls
    const calls = await getAllRows(
      `SELECT * FROM call_sessions ${whereClause}
       ORDER BY start_time DESC
       LIMIT ? OFFSET ?`,
      [...params, parseInt(limit), parseInt(offset)]
    );

    res.status(200).json({
      success: true,
      calls: calls.map(call => CallSession.fromJSON(call).getSummary()),
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total: countResult.total,
        pages: Math.ceil(countResult.total / limit)
      }
    });

  } catch (error) {
    logger.error('Failed to get call history:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get call history',
      message: error.message
    });
  }
});

/**
 * Get call details
 * GET /api/calls/:callId
 */
router.get('/:callId', authenticateToken, async (req, res) => {
  try {
    const { callId } = req.params;

    const call = await getRow(
      'SELECT * FROM call_sessions WHERE id = ? AND user_id = ?',
      [callId, req.user.userId]
    );

    if (!call) {
      return res.status(404).json({
        success: false,
        error: 'Call not found'
      });
    }

    res.status(200).json({
      success: true,
      call: CallSession.fromJSON(call).toJSON()
    });

  } catch (error) {
    logger.error('Failed to get call details:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get call details',
      message: error.message
    });
  }
});

/**
 * Get call statistics
 * GET /api/calls/stats
 */
router.get('/stats', authenticateToken, async (req, res) => {
  try {
    const { period = '30' } = req.query; // days

    const stats = await getRow(
      `SELECT 
        COUNT(*) as total_calls,
        COUNT(CASE WHEN status = 'connected' THEN 1 END) as connected_calls,
        COUNT(CASE WHEN direction = 'outbound' THEN 1 END) as outbound_calls,
        COUNT(CASE WHEN direction = 'inbound' THEN 1 END) as inbound_calls,
        AVG(duration) as avg_duration,
        SUM(duration) as total_duration
       FROM call_sessions 
       WHERE user_id = ? AND start_time >= datetime('now', '-${period} days')`,
      [req.user.userId]
    );

    // Calculate answer rate
    const answerRate = stats.total_calls > 0 ?
      ((stats.connected_calls / stats.total_calls) * 100).toFixed(2) : 0;

    res.status(200).json({
      success: true,
      stats: {
        ...stats,
        answer_rate: parseFloat(answerRate),
        period_days: parseInt(period)
      }
    });

  } catch (error) {
    logger.error('Failed to get call statistics:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to get call statistics',
      message: error.message
    });
  }
});

/**
 * Save call session
 * POST /api/calls/save
 */
router.post('/save', authenticateToken, async (req, res) => {
  try {
    const callData = req.body;
    const callSession = new CallSession(callData);

    // Validate call session
    const validation = callSession.validate();
    if (!validation.isValid) {
      return res.status(400).json({
        success: false,
        error: 'Invalid call data',
        details: validation.errors
      });
    }

    // Save to database
    await runQuery(
      `INSERT INTO call_sessions (
        id, user_id, from_number, to_number, protocol, direction, status,
        start_time, answer_time, end_time, duration, end_reason,
        transferred_to, transfer_time, hold_time, resume_time,
        session_data, metadata, recording, recording_path,
        ai_enabled, ai_data, quality, cost
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        callSession.id, req.user.userId, callSession.from, callSession.to,
        callSession.protocol, callSession.direction, callSession.status,
        callSession.startTime, callSession.answerTime, callSession.endTime,
        callSession.duration, callSession.endReason, callSession.transferredTo,
        callSession.transferTime, callSession.holdTime, callSession.resumeTime,
        JSON.stringify(callSession.sessionData), JSON.stringify(callSession.metadata),
        callSession.recording, callSession.recordingPath, callSession.aiEnabled,
        JSON.stringify(callSession.aiData), callSession.quality, callSession.cost
      ]
    );

    res.status(201).json({
      success: true,
      message: 'Call session saved successfully',
      callId: callSession.id
    });

  } catch (error) {
    logger.error('Failed to save call session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to save call session',
      message: error.message
    });
  }
});

/**
 * Update call session
 * PUT /api/calls/:callId
 */
router.put('/:callId', authenticateToken, async (req, res) => {
  try {
    const { callId } = req.params;
    const updates = req.body;

    // Check if call exists and belongs to user
    const existingCall = await getRow(
      'SELECT id FROM call_sessions WHERE id = ? AND user_id = ?',
      [callId, req.user.userId]
    );

    if (!existingCall) {
      return res.status(404).json({
        success: false,
        error: 'Call not found'
      });
    }

    // Build update query
    const updateFields = [];
    const params = [];

    Object.entries(updates).forEach(([key, value]) => {
      if (key !== 'id' && key !== 'user_id') {
        updateFields.push(`${key} = ?`);
        params.push(typeof value === 'object' ? JSON.stringify(value) : value);
      }
    });

    if (updateFields.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'No valid fields to update'
      });
    }

    updateFields.push('updated_at = CURRENT_TIMESTAMP');
    params.push(callId);

    await runQuery(
      `UPDATE call_sessions SET ${updateFields.join(', ')} WHERE id = ?`,
      params
    );

    res.status(200).json({
      success: true,
      message: 'Call session updated successfully'
    });

  } catch (error) {
    logger.error('Failed to update call session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to update call session',
      message: error.message
    });
  }
});

/**
 * Delete call session
 * DELETE /api/calls/:callId
 */
router.delete('/:callId', authenticateToken, async (req, res) => {
  try {
    const { callId } = req.params;

    // Check if call exists and belongs to user
    const existingCall = await getRow(
      'SELECT id FROM call_sessions WHERE id = ? AND user_id = ?',
      [callId, req.user.userId]
    );

    if (!existingCall) {
      return res.status(404).json({
        success: false,
        error: 'Call not found'
      });
    }

    // Delete related records first (due to foreign key constraints)
    await runQuery('DELETE FROM call_transcriptions WHERE call_id = ?', [callId]);
    await runQuery('DELETE FROM ai_responses WHERE call_id = ?', [callId]);
    await runQuery('DELETE FROM call_recordings WHERE call_id = ?', [callId]);
    await runQuery('DELETE FROM call_analytics WHERE call_id = ?', [callId]);

    // Delete call session
    await runQuery('DELETE FROM call_sessions WHERE id = ?', [callId]);

    res.status(200).json({
      success: true,
      message: 'Call session deleted successfully'
    });

  } catch (error) {
    logger.error('Failed to delete call session:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to delete call session',
      message: error.message
    });
  }
});

module.exports = router;
