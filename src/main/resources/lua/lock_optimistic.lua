--[[
乐观锁
参数:
lock_key,lock_second
返回值:
{locked}
-- ]]
local lock_key = KEYS[1]
local lock_second = tonumber(ARGV[1])

local exist = redis.call("EXISTS", lock_key)
local locked = 0
if exist == 0 then
    redis.call("SETEX", lock_key, lock_second, 1)
    locked = 1
end
return { locked }
