import request from './request'

export interface HomeHeroConfigResponseBody {
  success: boolean
  data?: {
    videoIds?: string[]
  }
}

export function fetchHomeHeroConfig() {
  return request.get<HomeHeroConfigResponseBody>('/api/admin/home-hero')
}

export function saveHomeHeroConfig(videoIds: string[]) {
  return request.post<{ success: boolean; message?: string }>('/api/admin/home-hero', {
    videoIds: videoIds || [],
  })
}

