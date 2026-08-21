@echo off
echo 测试旅程分析接口
echo.

set BASE_URL=http://localhost:8080/journey-analysis
set CONTENT_TYPE=Content-Type: application/json

echo 1. 测试各年龄段占比接口
curl -X POST "%BASE_URL%/getAgeDistribution" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 2. 测试所在区域占比接口
curl -X POST "%BASE_URL%/getRegionDistribution" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 3. 测试用户性别占比接口
curl -X POST "%BASE_URL%/getGenderDistribution" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 4. 测试用户类型占比接口
curl -X POST "%BASE_URL%/getUserTypeDistribution" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 5. 测试用户关注场景TOP10接口
curl -X POST "%BASE_URL%/getFocusSceneTop" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 6. 测试发声用户TOP5接口
curl -X POST "%BASE_URL%/getVoiceUserTop" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 7. 测试旅程细化分析接口（提及量）
curl -X POST "%BASE_URL%/getJourneyDetailAnalysis" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\",\"dataType\":\"mention\"}"
echo.
echo.

echo 8. 测试旅程细化分析接口（负面率）
curl -X POST "%BASE_URL%/getJourneyDetailAnalysis" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\",\"dataType\":\"negative_rate\"}"
echo.
echo.

echo 9. 测试高频场景TOP5接口
curl -X POST "%BASE_URL%/getHighFreqSceneTop" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 10. 测试飙升场景TOP5接口
curl -X POST "%BASE_URL%/getSurgingSceneTop" ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"startTime\":\"2024-01-01\",\"endTime\":\"2024-12-31\",\"clientId\":\"test\"}"
echo.
echo.

echo 测试完成！
pause
