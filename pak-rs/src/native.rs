use crate::zlib::ZStream;

/** Plain exports */

#[no_mangle]
pub unsafe extern "C" fn deflate_init(stream: ZStream, level: i32) -> i32 {
    stream.deflate_init(level)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_params_critical(stream: ZStream, next_in: *const u8, next_out: *mut u8,
                                                                  avail_in: u32, avail_out: u32,
                                                                  level: i32, strategy: i32) -> i32 {
    stream.deflate_params_ext(next_in, next_out, avail_in, avail_out, level, strategy)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_get_dictionary_critical(stream: ZStream, dictionary: *mut u8, length: *mut u32) -> i32 {
    stream.deflate_get_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_set_dictionary_critical(stream: ZStream, dictionary: *mut u8, length: u32) -> i32 {
    stream.deflate_set_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_critical(stream: ZStream, next_in: *const u8, next_out: *mut u8,
                                                           avail_in: u32, avail_out: u32,
                                                           flush: i32) -> i32 {
    stream.deflate_ext(next_in, next_out, avail_in, avail_out, flush)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_reset(stream: ZStream) -> i32 {
    stream.deflate_reset()
}

#[no_mangle]
pub unsafe extern "C" fn deflate_end(stream: ZStream) -> i32 {
    stream.deflate_end()
}

#[no_mangle]
pub unsafe extern "C" fn inflate_init(stream: ZStream) -> i32 {
    stream.inflate_init()
}

#[no_mangle]
pub unsafe extern "C" fn inflate_get_dictionary_critical(stream: ZStream, dictionary: *mut u8, length: *mut u32) -> i32 {
    stream.inflate_get_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_set_dictionary_critical(stream: ZStream, dictionary: *mut u8, length: u32) -> i32 {
    stream.inflate_set_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_critical(stream: ZStream, next_in: *const u8, next_out: *mut u8,
                                                      avail_in: u32, avail_out: u32,
                                                      flush: i32) -> i32 {
    stream.inflate_ext(next_in, next_out, avail_in, avail_out, flush)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_reset(stream: ZStream) -> i32 {
    stream.inflate_reset()
}

#[no_mangle]
pub unsafe extern "C" fn inflate_end(stream: ZStream) -> i32 {
    stream.inflate_end()
}
