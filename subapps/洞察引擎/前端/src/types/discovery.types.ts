export interface WordsInfo {
  wordName?: string
  currentFrequency?: string
  historyTotalFrequency?: string
  channelSource?: string
  systemSuggestedBusiness?: string
  systemSuggestedQuality?: string
  normalizedOpinions?: string
  correspondingOpinions?: string
  allocationRecord?: {
    tagType?: string
    tagCategoryName?: string
    operateTime?: string
    tagTypeText?: string
  }[]
}
