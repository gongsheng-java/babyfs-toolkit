--[[
更新指定counter key的last sync字段`_s`为last_write_version
参数:
last_write_version,sync_timestamp
返回值:
{updated}
-- ]]
local counter_key = KEYS[1] --计数器key
local last_write = tonumber(ARGV[1]) -- 完成同步的_w版本号
local sync_timestamp = tonumber(ARGV[2]) -- 同步的时间戳

local exist = redis.call("EXISTS", counter_key)
local updated = 0
if exist == 1 then
    local pre = redis.call("HGET", counter_key, "_s")
    if not pre or last_write > tonumber(pre) then
        redis.call("HMSET", counter_key, "_s", last_write, "_st", sync_timestamp)
        updated = 1
    end
end
return { updated }
