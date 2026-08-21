export interface BreadcrumbItem {
  name: string
  code?: string
  [key: string]: any
}

export interface FDdbreadcrumbProps {
  breadcrumbList?: BreadcrumbItem[]
  suffix?: string
}

export interface FDdbreadcrumbEmits {
  (e: 'breadcrumb-click', item: BreadcrumbItem, index: number): void
}
