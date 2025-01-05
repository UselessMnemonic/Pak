use core::ffi::c_void;
use core::ffi::c_ulong;
use core::ffi::c_uint;
use core::ffi::c_int;
use core::ptr::null_mut;

type ZAlloc = unsafe extern "C" fn(opaque: *const c_void, items: c_uint, size: c_uint) -> *const c_void;
type ZFree = unsafe extern "C" fn(opaque: *const c_void, address: *const c_void) -> c_void;

#[repr(C)]
pub struct ZStream {
    pub next_in: *mut u8,
    pub avail_in: c_uint,
    pub total_in: c_ulong,

    pub next_out: *mut u8,
    pub avail_out: c_uint,
    pub total_out: c_ulong,

    pub msg: *const u8,
    pub state: *const c_void,

    pub zalloc: Option<ZAlloc>,
    pub zfree: Option<ZFree>,
    pub opaque: *const c_void,

    pub data_type: c_uint,
    pub adler: c_ulong,
    pub reserved: c_ulong,
}

type ZAction = unsafe extern "C" fn(stream: *mut ZStream, flush: c_int) -> c_int;
type ZDeflateParams = unsafe extern "C" fn(stream: *mut ZStream, level: c_int, strategy: c_int) -> c_int;

#[no_mangle]
pub unsafe extern "C" fn criticalAction(action: ZAction, stream: *mut ZStream, next_in: *mut u8, next_out: *mut u8,
                                        avail_in: c_uint, avail_out: c_uint, flush: c_int) -> c_int {
    (*stream).next_in = next_in;
    (*stream).avail_in = avail_in;
    (*stream).next_out = next_out;
    (*stream).avail_out = avail_out;
    let result = action(stream, flush);

    (*stream).next_in = null_mut();
    (*stream).avail_in = 0;
    (*stream).next_out = null_mut();
    (*stream).avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn criticalDeflateParams(deflate_params: ZDeflateParams, stream: *mut ZStream, next_in: *mut u8,
                                               next_out: *mut u8, avail_in: c_uint, avail_out: c_uint, level: c_int,
                                               strategy: c_int) -> c_int {
    (*stream).next_in = next_in;
    (*stream).avail_in = avail_in;
    (*stream).next_out = next_out;
    (*stream).avail_out = avail_out;
    let result = deflate_params(stream, level, strategy);

    (*stream).next_in = null_mut();
    (*stream).avail_in = 0;
    (*stream).next_out = null_mut();
    (*stream).avail_out = 0;
    return result;
}
