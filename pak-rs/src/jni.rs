use crate::zlib::ZStream;
use std::os::raw::c_void;
use std::sync::OnceLock;
use jni::errors::Error;
use jni::strings::JNIString;
use jni::sys::JNI_ERR;
use jni::{JNIEnv, JNIVersion, JavaVM, NativeMethod};
use jni::objects::{GlobalRef, JByteArray, JClass, JObject};

struct JNIContext {
    pub receiver_class: GlobalRef,
}

static mut CONTEXT: OnceLock<JNIContext> = OnceLock::new();

#[no_mangle]
pub extern "C" fn JNI_OnLoad(jvm: JavaVM, _: *const c_void) -> i32 {
    let env = match jvm.get_env() {
        Ok(it) => it,
        Err(_) => return JNI_ERR
    };
    match on_load(env) {
        Ok(_) => JNIVersion::V6.into(),
        Err(_) => JNI_ERR
    }
}

#[no_mangle]
pub extern "C" fn JNI_OnUnload(jvm: JavaVM, _: *const c_void) {
    let env = match jvm.get_env() {
        Ok(it) => it,
        Err(_) => return
    };
    _ = match unsafe { CONTEXT.take() } {
        Some(it) => it,
        None => return
    };
}

extern "C" fn new_stream(_: JNIEnv, _: JClass) -> u64 {
    ZStream::new_raw() as usize as u64
}

unsafe extern "C" fn delete_stream(_: JNIEnv, _: JClass, handle: u64) {
    let raw = handle as usize as *mut ZStream;
    if raw.is_null() {
        return;
    }
    _ = Box::from_raw(raw);
}

fn on_load(env: JNIEnv) -> Result<(), Error> {
    let receiver_class = env.find_class("com/uselessmnemonic/pak/ZStreamRefImpl")?;
    let receiver_class = env.new_global_ref(receiver_class)?;

    let methods = [
        NativeMethod { name: JNIString::from("newStream"), sig: JNIString::from("()L"), fn_ptr: new_stream as *mut c_void },
        NativeMethod { name: JNIString::from("deleteStream"), sig: JNIString::from("(L)V"), fn_ptr: delete_stream as *mut c_void }
    ];

    env.register_native_methods(receiver_class, &methods)?;
    CONTEXT.set(JNIContext {
        receiver_class
    });
    Ok(())
}
