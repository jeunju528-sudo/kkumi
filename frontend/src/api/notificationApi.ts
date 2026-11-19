import type { NotificationResponse, UnreadCountResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getNotifications(): Promise<NotificationResponse[]> {
    const res = await fetch('/api/notifications')
    if (!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<NotificationResponse[]>
}

export async function getUnreadCount(): Promise<UnreadCountResponse> {
    const res = await fetch('/api/notifications/unread-count')
    if (!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<UnreadCountResponse>
}

export async function readNotification(id: number): Promise<void> {
    const res = await fetch(`/api/notifications/${id}/read`, { method: 'POST' })
    if (!res.ok) {
        throw await toApiError(res)
    }
}

export async function readAllNotifications(): Promise<void> {
    const res = await fetch('/api/notifications/read-all', { method: 'POST' })
    if (!res.ok) {
        throw await toApiError(res)
    }
}

export async function deleteNotification(id: number): Promise<void> {
    const res = await fetch(`/api/notifications/${id}`, { method: 'DELETE' })
    if (!res.ok) {
        throw await toApiError(res)
    }
}