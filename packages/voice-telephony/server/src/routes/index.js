/**
 * Telephony routes index — aggregates all sub-routers
 */
const express = require('express')
const router = express.Router()

router.use('/auth', require('./auth'))
router.use('/calls', require('./calls'))
router.use('/dialer', require('./dialer'))
router.use('/ivr', require('./ivr'))
router.use('/carrier', require('./carrier'))
router.use('/ai', require('./ai'))
router.use('/agent', require('./agent'))

module.exports = router
