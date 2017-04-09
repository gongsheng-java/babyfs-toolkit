--[[
淘汰指定的counter key
参数:
sync_set_key,last_write_version
返回值:
{evicted}
-- ]]
local counter_key = KEYS[1] --计数器key
local sync_set_key = ARGV[1] -- 同步集合key
local last_write = tonumber(ARGV[2]) -- 最后一次同步的_w版本号

local exist = redis.call("EXISTS", counter_key)
local evicted = 0
if exist == 1 then
    local pre = redis.call("HGET", counter_key, "_w")
    if pre and last_write == tonumber(pre) then
        redis.call("DEL", counter_key)
        evicted = 1
    end
else
    evicted = 1
end

-- 如果已经淘汰,则将其从sync_set中删除
if evicted == 1 then
    redis.call("ZREM", sync_set_key, counter_key)
end

return { evicted }
