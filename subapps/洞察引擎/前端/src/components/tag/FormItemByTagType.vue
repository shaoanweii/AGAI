<template>
  <el-form-item
    v-if="keys.includes(ENERGY_TYPE)"
    prop="energyType"
    label="关联能源分类"
    required
    :wrapper-col-style="{}"
  >
    <el-checkbox
      :data-testid="`${testid}form-item-10001`"
      :model-value="checkedAll(ENERGY_TYPE)"
      :indeterminate="indeterminate(ENERGY_TYPE)"
      @change="(val: any) => handleChangeAll(val, ENERGY_TYPE)"
      style="margin: -2px 16px 0 0"
      >全部
    </el-checkbox>
    <el-checkbox-group :data-testid="`${testid}form-item-10002`" v-model="formData.energyType">
      <el-checkbox
        v-for="(item, index) of configItem[ENERGY_TYPE]"
        :key="index"
        :data-testid="`${testid}form-item-10002-${index}`"
        :value="item.classifyCode"
        >{{ item.classifyName }}
      </el-checkbox>
    </el-checkbox-group>
  </el-form-item>
  <el-form-item
    v-if="keys.includes(CAR_TYPE)"
    prop="carType"
    label="关联车辆类型"
    required
    label-width="184px"
    style="width: 1000px"
  >
    <el-checkbox
      :data-testid="`${testid}form-item-10003`"
      :style="{ flex: 'none' }"
      :model-value="checkedAll(CAR_TYPE)"
      :indeterminate="indeterminate(CAR_TYPE)"
      @change="(val: any) => handleChangeAll(val, CAR_TYPE)"
      style="margin: -2px 16px 0 0"
      >全部
    </el-checkbox>
    <el-checkbox-group v-model="formData.carType" :data-testid="`${testid}form-item-10004`">
      <el-checkbox
        v-for="(item, index) of configItem[CAR_TYPE]"
        :key="index"
        :data-testid="`${testid}form-item-10005-${index}`"
        :value="item.classifyCode"
        >{{ item.classifyName }}
      </el-checkbox>
    </el-checkbox-group>
  </el-form-item>
  <el-form-item v-if="keys.includes(SERIOUSNESS)" prop="seriousness" label="严重性等级" required>
    <el-radio-group
      :data-testid="`${testid}form-item-10006`"
      v-model="formData.seriousness"
      default-value="Default"
    >
      <el-radio
        v-for="(item, index) of configItem[SERIOUSNESS]"
        :key="index"
        :data-testid="`${testid}form-item-10007-${index}`"
        :value="item.classifyCode"
        >{{ item.classifyName }}
      </el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item v-if="keys.includes(USER_JOURNEY)" prop="userJourney" label="关联用户旅程" required>
    <el-checkbox
      :data-testid="`${testid}form-item-10008`"
      :model-value="checkedAll(USER_JOURNEY)"
      :indeterminate="indeterminate(USER_JOURNEY)"
      @change="(val: any) => handleChangeAll(val, USER_JOURNEY)"
      style="margin: -2px 16px 0 0"
      >全部
    </el-checkbox>
    <el-checkbox-group v-model="formData.userJourney" :data-testid="`${testid}form-item-10009`">
      <el-checkbox
        v-for="(item, index) of configItem[USER_JOURNEY]"
        :key="index"
        :data-testid="`${testid}form-item-10010-${index}`"
        :value="item.classifyCode"
        >{{ item.classifyName }}
      </el-checkbox>
    </el-checkbox-group>
  </el-form-item>
</template>

<script setup lang="ts">
import type { ConditionsByType, ConditionsDetailItem } from '@/types'

/**
 * 能源类型,energy_type
 * 车辆类型,car_type
 * 严重性,seriousness
 * 用户旅程,user_journey
 */
const ENERGY_TYPE = 'energy_type'
const CAR_TYPE = 'car_type'
const SERIOUSNESS = 'seriousness'
const USER_JOURNEY = 'user_journey'

const props = withDefaults(
  defineProps<{
    conditions: Record<string, ConditionsDetailItem[]>
    formData: Record<any, any>
    configItem: Record<any, ConditionsByType[]>
    testid?: string
  }>(),
  {}
)
const { formData, configItem } = toRefs(props)

const keys = computed(() => {
  if (!configItem.value) return []
  return Object.keys(configItem.value)
})

// 全选
const checkedAll = computed(() => {
  return (field: string) => {
    switch (field) {
      case ENERGY_TYPE:
        return formData.value.energyType?.length === configItem.value[ENERGY_TYPE]?.length
      case CAR_TYPE:
        return formData.value.carType?.length === configItem.value[CAR_TYPE]?.length
      case USER_JOURNEY:
        return formData.value.userJourney?.length === configItem.value[USER_JOURNEY]?.length
    }
  }
})
// 半选
const indeterminate = computed(() => {
  return (field: string) => {
    switch (field) {
      case ENERGY_TYPE:
        const energyTypeLength = formData.value.energyType?.length
        return !(
          !energyTypeLength ||
          energyTypeLength === 0 ||
          energyTypeLength === configItem.value[ENERGY_TYPE]?.length
        )
      case CAR_TYPE:
        const carTypeLength = formData.value.carType?.length
        return !(
          !carTypeLength ||
          carTypeLength === 0 ||
          carTypeLength === configItem.value[CAR_TYPE]?.length
        )
      case USER_JOURNEY:
        const userJourneyLength = formData.value.userJourney?.length
        return !(
          !userJourneyLength ||
          userJourneyLength === 0 ||
          userJourneyLength === configItem.value[USER_JOURNEY]?.length
        )
    }
  }
})

const checkAllFn = (field: string) => {
  switch (field) {
    case ENERGY_TYPE:
      formData.value.energyType = configItem.value[ENERGY_TYPE]?.map(el => el.classifyCode)
      break
    case CAR_TYPE:
      formData.value.carType = configItem.value[CAR_TYPE]?.map(el => el.classifyCode)
      break
    case USER_JOURNEY:
      formData.value.userJourney = configItem.value[USER_JOURNEY]?.map(el => el.classifyCode)
      break
  }
}

const clearCheckAllFn = (field: string) => {
  switch (field) {
    case ENERGY_TYPE:
      formData.value.energyType = []
      break
    case CAR_TYPE:
      formData.value.carType = []
      break
    case USER_JOURNEY:
      formData.value.userJourney = []
      break
  }
}

const handleChangeAll = (val: boolean, field: string) => {
  if (val) {
    checkAllFn(field)
  } else {
    clearCheckAllFn(field)
  }
}
</script>

<style scoped lang="scss"></style>
