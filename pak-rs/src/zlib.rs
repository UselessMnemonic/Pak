use std::ffi::{c_char, c_ulong, c_void, OsString};
use std::marker::PhantomData;
use std::env;
use std::ops::Deref;
use std::sync::LazyLock;
use libloading::Library;

/** Bindings for ZLib */

type ZAllocFn = unsafe extern "C" fn(opaque: *mut c_void, items: u32, size: u32) -> *const c_void;
type ZFreeFn = unsafe extern "C" fn(opaque: *mut c_void, address: *mut c_void) -> c_void;
type ZLibVersionFn = unsafe extern "C" fn() -> *const c_char;

#[repr(C)]
#[derive(Debug)]
pub struct ZStream<'s> {
    pub next_in: *const u8,
    pub avail_in: u32,
    pub total_in: c_ulong,

    pub next_out: *mut u8,
    pub avail_out: u32,
    pub total_out: c_ulong,

    pub msg: *const u8,
    pub state: *const c_void,

    pub zalloc: Option<ZAllocFn>,
    pub zfree: Option<ZFreeFn>,
    pub opaque: *const c_void,

    pub data_type: u32,
    pub adler: c_ulong,
    pub reserved: c_ulong,
    marker: PhantomData<&'s ()>
}

type ZDeflateInitFn = unsafe extern "C" fn(stream: *mut ZStream, level: i32, version: *const c_char, size: u32) -> i32;
type ZDeflateParamsFn = unsafe extern "C" fn(stream: *mut ZStream, level: i32, strategy: i32) -> i32;
type ZDeflateGetDictionaryFn = unsafe extern "C" fn(stream: *mut ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZDeflateSetDictionaryFn = unsafe extern "C" fn(stream: *mut ZStream, dictionary: *const u8, size: u32) -> i32;
type ZDeflateFn = unsafe extern "C" fn(stream: *mut ZStream, flush: i32) -> i32;
type ZDeflateResetFn = unsafe extern "C" fn(stream: *mut ZStream) -> i32;
type ZDeflateEndFn = unsafe extern "C" fn(stream: *mut ZStream) -> i32;

type ZInflateInitFn = unsafe extern "C" fn(stream: *mut ZStream, version: *const c_char, size: u32) -> i32;
type ZInflateGetDictionaryFn = unsafe extern "C" fn(stream: *mut ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZInflateSetDictionaryFn = unsafe extern "C" fn(stream: *mut ZStream, dictionary: *const u8, size: u32) -> i32;
type ZInflateFn = unsafe extern "C" fn(stream: *mut ZStream, flush: i32) -> i32;
type ZInflateResetFn = unsafe extern "C" fn(stream: *mut ZStream) -> i32;
type ZInflateEndFn = unsafe extern "C" fn(stream: *mut ZStream) -> i32;

#[derive(Debug)]
struct ZLib<'z> {
    library: Library,
    zlib_version: *const c_char,

    deflate_init: ZDeflateInitFn,
    deflate_params: ZDeflateParamsFn,
    deflate_get_dictionary: ZDeflateGetDictionaryFn,
    deflate_set_dictionary: ZDeflateSetDictionaryFn,
    deflate: ZDeflateFn,
    deflate_reset: ZDeflateResetFn,
    deflate_end: ZDeflateEndFn,

    inflate_init: ZInflateInitFn,
    inflate_get_dictionary: ZInflateGetDictionaryFn,
    inflate_set_dictionary: ZInflateSetDictionaryFn,
    inflate: ZInflateFn,
    inflate_reset: ZInflateResetFn,
    inflate_end: ZInflateEndFn,

    _owns_library: PhantomData<&'z Library>
}

impl<'z> ZLib<'z> {
    fn new() -> ZLib<'z> {
        let path = match env::var("ZLIB_LIBRARY") {
            Ok(path) => OsString::from(path),
            Err(cause) => match cause {
                #[cfg(target_os = "windows")]
                env::VarError::NotPresent => OsString::from("zlib1"),
                #[cfg(not(target_os = "windows"))]
                env::VarError::NotPresent => OsString::from("libz"),
                env::VarError::NotUnicode(it) => it
            }
        };
        unsafe {
            let library = Library::new(path).unwrap();
            let zlib_version = library.get::<ZLibVersionFn>(b"zlibVersion\0").unwrap()();

            let deflate_init = library.get::<ZDeflateInitFn>(b"deflateInit_\0").unwrap().deref().to_owned();
            let deflate_params = library.get::<ZDeflateParamsFn>(b"deflateParams\0").unwrap().deref().to_owned();
            let deflate_get_dictionary = library.get::<ZDeflateGetDictionaryFn>(b"deflateGetDictionary\0").unwrap().deref().to_owned();
            let deflate_set_dictionary = library.get::<ZDeflateSetDictionaryFn>(b"deflateSetDictionary\0").unwrap().deref().to_owned();
            let deflate = library.get::<ZDeflateFn>(b"deflate\0").unwrap().deref().to_owned();
            let deflate_reset = library.get::<ZDeflateResetFn>(b"deflateReset\0").unwrap().deref().to_owned();
            let deflate_end = library.get::<ZDeflateEndFn>(b"deflateEnd\0").unwrap().deref().to_owned();
    
            let inflate_init = library.get::<ZInflateInitFn>(b"inflateInit_\0").unwrap().deref().to_owned();
            let inflate_get_dictionary = library.get::<ZInflateGetDictionaryFn>(b"inflateGetDictionary\0").unwrap().deref().to_owned();
            let inflate_set_dictionary = library.get::<ZInflateSetDictionaryFn>(b"inflateSetDictionary\0").unwrap().deref().to_owned();
            let inflate = library.get::<ZInflateFn>(b"inflate\0").unwrap().deref().to_owned();
            let inflate_reset = library.get::<ZInflateResetFn>(b"inflateReset\0").unwrap().deref().to_owned();
            let inflate_end = library.get::<ZInflateEndFn>(b"inflateEnd\0").unwrap().deref().to_owned();

            ZLib {
                library,
                zlib_version,
                deflate_init,
                deflate_params,
                deflate_get_dictionary,
                deflate_set_dictionary,
                deflate,
                deflate_reset,
                deflate_end,
                inflate_init,
                inflate_get_dictionary,
                inflate_set_dictionary,
                inflate,
                inflate_reset,
                inflate_end,
                _owns_library: PhantomData {}
            }
        }
    }
}

unsafe impl<'z> Sync for ZLib<'z> {}

unsafe impl<'z> Send for ZLib<'z> {}

static ZLIB: LazyLock<ZLib<'static>> = LazyLock::new(ZLib::new);

impl<'s> ZStream<'s> {

    pub unsafe fn deflate_init(&mut self, level: i32) -> i32 {
        (ZLIB.deflate_init)(self, level, ZLIB.zlib_version, size_of::<ZStream>() as u32)
    }

    pub unsafe fn deflate_params(&mut self, level: i32, strategy: i32) -> i32 {
        (ZLIB.deflate_params)(self, level, strategy)
    }

    pub unsafe fn deflate_get_dictionary(&mut self, dictionary: *mut u8, size: *mut u32) -> i32 {
        (ZLIB.deflate_get_dictionary)(self, dictionary, size)
    }

    pub unsafe fn deflate_set_dictionary(&mut self, dictionary: *const u8, size: u32) -> i32 {
        (ZLIB.deflate_set_dictionary)(self, dictionary, size)
    }

    pub unsafe fn deflate(&mut self, flush: i32) -> i32 {
        (ZLIB.deflate)(self, flush)
    }

    pub unsafe fn deflate_reset(&mut self) -> i32 {
        (ZLIB.deflate_reset)(self)
    }

    pub unsafe fn deflate_end(&mut self) -> i32 {
        (ZLIB.deflate_end)(self)
    }

    pub unsafe fn inflate_init(&mut self) -> i32 {
        (ZLIB.inflate_init)(self, ZLIB.zlib_version, size_of::<ZStream>() as u32)
    }

    pub unsafe fn inflate_get_dictionary(&mut self, dictionary: *mut u8, size: *mut u32) -> i32 {
        (ZLIB.inflate_get_dictionary)(self, dictionary, size)
    }

    pub unsafe fn inflate_set_dictionary(&mut self, dictionary: *const u8, size: u32) -> i32 {
        (ZLIB.inflate_set_dictionary)(self, dictionary, size)
    }

    pub unsafe fn inflate(&mut self, flush: i32) -> i32 {
        (ZLIB.inflate)(self, flush)
    }

    pub unsafe fn inflate_reset(&mut self) -> i32 {
        (ZLIB.inflate_reset)(self)
    }

    pub unsafe fn inflate_end(&mut self) -> i32 {
        (ZLIB.inflate_end)(self)
    }
}
