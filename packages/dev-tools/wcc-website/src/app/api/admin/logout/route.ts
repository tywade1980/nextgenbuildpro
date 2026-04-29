import { NextResponse } from 'next/server'
import { cookies } from 'next/headers'

export async function POST() {
  const cookieStore = await cookies()
  cookieStore.delete('wcc_admin_session')
  return NextResponse.json({ ok: true })
}
