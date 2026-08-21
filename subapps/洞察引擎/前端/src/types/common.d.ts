declare namespace CommonType {
  namespace hooks {
    type PageName = 'founding' | 'processing' | 'discovery' | 'baseSettings' | 'insDataSource'

    type PermKeyItem = { perm: string; key: string }

    interface TPPage {
      defaultActive: string
      permKeyMap: Map<string, PermKeyItem>
    }
  }
}
