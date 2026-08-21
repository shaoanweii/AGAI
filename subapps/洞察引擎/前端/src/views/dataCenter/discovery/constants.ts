export const NEW_WORD_DETAILS_DIALOG_TAB = {
  OPERATION: 'operation',
  DETAILS: 'details'
} as const

export type NewWordDetailsDialogTab =
  (typeof NEW_WORD_DETAILS_DIALOG_TAB)[keyof typeof NEW_WORD_DETAILS_DIALOG_TAB]
