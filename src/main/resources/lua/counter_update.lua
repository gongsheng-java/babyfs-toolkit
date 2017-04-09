--[[
更新或者初始化计数器
参数:
sync_set_key,is_init,field1,field1_delta,...
返回值:
{exist,updated}
-- ]]
local counter_key = KEYS[1] --计数器key
local sync_set_key = ARGV[1] -- 同步集合key
local last_write_timestamp = tonumber(ARGV[2]) -- 最后一次写入的时间戳
local is_init = tonumber(ARGV[3]) -- 是否初始化操作

-- 检查field和delta参数是否合法
if #ARGV < 5 or (#ARGV - 3) % 2 ~= 0 then
    return redis.error_reply("Wrong field and delta")
end

for i = 5, #ARGV, 2 do
    if not tonumber(ARGV[i]) then
        return redis.error_reply("Wrong delta value @" .. (i / 2))
    end
end

-- 解析字段增量和字段初始值
local field_deltas = {} -- 字段增量
local field_vals = {} -- 字段初始值
for i = 4, #ARGV, 2 do
    local prefix = string.sub(ARGV[i], 1, 1)
    if prefix == "_" then
        field_vals[#field_vals + 1] = string.sub(ARGV[i], 2)
        field_vals[#field_vals + 1] = ARGV[i + 1]
    else
        field_deltas[#field_deltas + 1] = ARGV[i]
        field_deltas[#field_deltas + 1] = ARGV[i + 1]
    end
end

local field_delta_len = #field_deltas
local field_val_len = #field_vals
local exist = redis.call("EXISTS", counter_key)
local updated = 0
if exist == 1 then
    -- key存在,1.更新各个field计数;2.更新修改时间戳
    if field_delta_len >= 2 then
        for i = 1, field_delta_len, 2 do
            redis.call("HINCRBY", counter_key, field_deltas[i], field_deltas[i + 1])
        end
        -- 增加_w版本号
        redis.call("HINCRBY", counter_key, "_w", 1)
        updated = 1
    end
else
    -- key不存在: 如果是初始化操作,初始化计数器并增加delta;否则返回不存在
    if is_init == 1 then
        if field_val_len > 0 then
            redis.call("HMSET", counter_key, "_w", 0, "_s", 0, "_st", last_write_timestamp, unpack(field_vals))
        else
            redis.call("HMSET", counter_key, "_w", 0, "_s", 0, "_st", last_write_timestamp)
        end
        exist = 1
        if field_delta_len >= 2 then
            for i = 1, field_delta_len, 2 do
                redis.call("HINCRBY", counter_key, field_deltas[i], field_deltas[i + 1])
            end
            -- 增加_w版本号
            redis.call("HINCRBY", counter_key, "_w", 1)
            updated = 1
        end
    end
end

-- 如果counter_key存在,则将counter_key记录到sync_set
if exist == 1 then
    local cur_access = redis.call("ZSCORE", sync_set_key, counter_key)
    if not cur_access or last_write_timestamp > tonumber(cur_access) then
        redis.call("ZADD", sync_set_key, last_write_timestamp, counter_key)
    end
end
return { exist, updated }
