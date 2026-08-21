/**
 * 设置高级筛选默认值
 * 处理时间维度
 */

import { defineStore } from 'pinia'

interface H5AppState {
  //首页的默认筛选项
  dateUnitInfo: {
    isDef: boolean
    dateUnit: number | undefined
    dateTime: any
  }
  defBrandCode: string | undefined
  //任务的默认筛选项
  dateTaskUnitInfo: {
    isDef: boolean
    dateUnit: number | undefined
    dateTime: any
  }
  defTaskBrandCode: string | undefined
  // 看数广场默认品牌
  defDataSquareBrandCode: string | undefined
}

export const useH5AppStore = defineStore('h5App', {
  state: (): H5AppState => ({
    dateUnitInfo: {
      isDef: false,
      dateUnit: undefined,
      dateTime: undefined
    },
    defBrandCode: undefined,
    dateTaskUnitInfo: {
      isDef: false,
      dateUnit: undefined,
      dateTime: undefined
    },
    defTaskBrandCode: undefined,
    defDataSquareBrandCode: undefined
  }),

  getters: {
    getDateUnitInfo(): H5AppState['dateUnitInfo'] {
      return this.dateUnitInfo
    },
    getDefBrandCode(): H5AppState['defBrandCode'] {
      return this.defBrandCode
    },
    getDateTaskUnitInfo(): H5AppState['dateTaskUnitInfo'] {
      return this.dateTaskUnitInfo
    },
    getDefTaskBrandCode(): H5AppState['defTaskBrandCode'] {
      return this.defTaskBrandCode
    },
    getDefDataSquareBrandCode(): H5AppState['defDataSquareBrandCode'] {
      return this.defDataSquareBrandCode
    }
  },

  actions: {
    /**
     * 解析时间维度信息
     */
    parseTimeDimension(jsonObject: any[], timeDimension: any[]): any {
      const dateField = jsonObject.find((el: any) => el.filterType === '94')
      let dateTime: any = null
      if (dateField) {
        const dateTimeList = timeDimension.find((el: any) => el.code === Number(dateField.value[0]))
        if (dateTimeList?.child && dateTimeList?.child?.length) {
          dateTime = dateTimeList?.child.find((el: any) => el.code === Number(dateField.value[1]))
        } else {
          dateTime = dateTimeList
        }
        return {
          isDef: true,
          dateUnit: Number(dateField.value[0]),
          dateTime: dateTime
        }
      }
      return {
        isDef: false,
        dateUnit: undefined,
        dateTime: undefined
      }
    },
    // 设置时间维度
    setDateUnit(jsonObject: any[], timeDimension: any[]) {
      this.dateUnitInfo = this.parseTimeDimension(jsonObject, timeDimension)
    },
    // 设置默认品牌
    setDefaultBrandCode(jsonObject: any[]) {
      const field = jsonObject.find((el: any) => el.filterType === '91')
      if (field) {
        this.defBrandCode = field.selected[0]
      }
    },
    // 获取任务时间维度
    setDateTaskUnit(jsonObject: any[], timeDimension: any[]) {
      this.dateTaskUnitInfo = this.parseTimeDimension(jsonObject, timeDimension)
    },
    // 设置默认任务品牌
    setDefaultTaskBrandCode(jsonObject: any[]) {
      const field = jsonObject.find((el: any) => el.filterType === '91')
      if (field) {
        this.defTaskBrandCode = field.selected[0]
      }
    },
    // 设置看数广场默认品牌
    setDefaultDataSquareBrandCode(jsonObject: any[]) {
      const field = jsonObject.find((el: any) => el.filterType === '91')
      if (field) {
        this.defDataSquareBrandCode = field.selected[0]
      }
    },
    setDefaultJsonObject(menus: any[], timeDimension: any[]) {
      const jsonObject = menus.find((el: any) => el.permissionKey === 'H5Home')?.jsonObject
      if (jsonObject?.length) {
        this.setDateUnit(jsonObject, timeDimension)
        this.setDefaultBrandCode(jsonObject)
      }
      const jsonObjectTask = menus.find((el: any) => el.permissionKey === 'H5TaskEvent')?.jsonObject
      if (jsonObjectTask?.length) {
        this.setDateTaskUnit(jsonObjectTask, timeDimension)
        this.setDefaultTaskBrandCode(jsonObjectTask)
      }
      const jsonObjectDataSquare = menus.find(
        (el: any) => el.permissionKey === 'H5DataPlaza'
      )?.jsonObject
      if (jsonObjectDataSquare?.length) {
        this.setDefaultDataSquareBrandCode(jsonObjectDataSquare)
      }
    }
  }
})
