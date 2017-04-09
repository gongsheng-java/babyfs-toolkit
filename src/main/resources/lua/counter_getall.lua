--[[
获取指定的counter key,并记录访问时间
参数:
sync_set_key,last_access_timestamp
返回值:
{evicted}
-- ]]
local counter_key = KEYS[1] --计数器key
local sync_set_key = ARGV[1] -- 同步集合key
local last_read = tonumber(ARGV[2]) -- 读取的时间戳

local pre = redis.call("HGETALL", counter_key)
if #pre > 0 then
    local cur_access = redis.call("ZSCORE", sync_set_key, counter_key)
    -- 更新记录
    if not cur_access or last_read > tonumber(cur_access) then
        redis.call("ZADD", sync_set_key, last_read, counter_key)
    end
end
return pre
