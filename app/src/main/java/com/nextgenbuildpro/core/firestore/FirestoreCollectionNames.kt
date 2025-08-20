package com.nextgenbuildpro.core.firestore

/**
 * Constants for Firestore collection names
 * This helps ensure consistency in collection names across the app
 */
object FirestoreCollectionNames {
    // Main collections
    const val LEADS = "leads"
    const val ESTIMATES = "estimates"
    const val PROJECTS = "projects"
    const val CLIENTS = "clients"
    const val TASKS = "tasks"
    const val USERS = "users"
    const val SETTINGS = "settings"
    const val CALENDAR_EVENTS = "calendar_events"

    // Sub-collections
    const val NOTES = "notes"
    const val PHOTOS = "photos"
    const val ATTACHMENTS = "attachments"
    const val MESSAGES = "messages"
    const val ACTIVITIES = "activities"

    // Nested sub-collections
    const val COMMENTS = "comments"
    const val TAGS = "tags"
}
