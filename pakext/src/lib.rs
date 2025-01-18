mod zlib;

use zlib::ZStream;
use std::ptr::null_mut;

#[no_mangle]
pub unsafe extern "C" fn deflate_init(stream: *mut ZStream, level: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.deflate_init(level)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_params_critical(stream: *mut ZStream,
                                                 next_in: *const u8, next_out: *mut u8,
                                                 avail_in: u32, avail_out: u32,
                                                 level: i32, strategy: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.deflate_params(level, strategy);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn deflate_get_dictionary_critical(stream: *mut ZStream, dictionary: *mut u8, length: *mut u32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.deflate_get_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_set_dictionary_critical(stream: *mut ZStream, dictionary: *mut u8, length: u32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.deflate_set_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn deflate_critical(stream: *mut ZStream,
                                          next_in: *const u8, next_out: *mut u8,
                                          avail_in: u32, avail_out: u32,
                                          flush: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.deflate(flush);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn deflate_reset(stream: *mut ZStream) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.deflate_reset()
}

#[no_mangle]
pub unsafe extern "C" fn deflate_end(stream: *mut ZStream) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.deflate_end()
}

#[no_mangle]
pub unsafe extern "C" fn inflate_init(stream: *mut ZStream, level: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.inflate_init(level)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_get_dictionary_critical(stream: *mut ZStream, dictionary: *mut u8, length: *mut u32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.inflate_get_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_set_dictionary_critical(stream: *mut ZStream, dictionary: *mut u8, length: u32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.inflate_set_dictionary(dictionary, length)
}

#[no_mangle]
pub unsafe extern "C" fn inflate_critical(stream: *mut ZStream,
                                          next_in: *const u8, next_out: *mut u8,
                                          avail_in: u32, avail_out: u32,
                                          flush: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.inflate(flush);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn inflate_reset(stream: *mut ZStream) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.inflate_reset()
}

#[no_mangle]
pub unsafe extern "C" fn inflate_end(stream: *mut ZStream) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.inflate_end()
}
